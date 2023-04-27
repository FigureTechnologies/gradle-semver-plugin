/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.git

import com.figure.gradle.semver.internal.exceptions.GitException
import com.figure.gradle.semver.internal.exceptions.UnexpectedException
import com.figure.gradle.semver.internal.githubActionsBuild
import com.figure.gradle.semver.internal.pullRequestEvent
import com.figure.gradle.semver.internal.pullRequestHeadRef
import com.figure.gradle.semver.internal.semverError
import com.figure.gradle.semver.internal.semverInfo
import com.figure.gradle.semver.internal.semverWarn
import net.swiftzer.semver.SemVer
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

internal fun Project.git(gitDir: String): Git =
    Git(
        FileRepositoryBuilder()
            .setGitDir(file(gitDir))
            .readEnvironment()
            .findGitDir()
            .build()
    )

internal fun Git.tagMap(prefix: String): Map<ObjectId, SemVer> {
    return tagList().call().toList()
        .mapNotNull { ref ->
            ref.semverTag(prefix)?.let { semver ->
                (repository.refDatabase.peel(ref).peeledObjectId ?: ref.objectId) to semver
            }
        }.toMap()
}

private fun Ref?.semverTag(prefix: String): SemVer? {
    return this?.name?.semverTag(prefix)
}

internal fun String?.semverTag(prefix: String): SemVer? =
    this?.substringAfterLast("/$prefix")?.let { semver ->
        if (semver.isNotBlank() && semver.count { letter -> letter == '.' } == 2) {
            runCatching { SemVer.parse(semver) }.getOrNull()
        } else {
            null
        }
    }

internal fun Git.currentBranchRef(): String? {
    return if (githubActionsBuild() && pullRequestEvent()) {
        pullRequestHeadRef()?.let { ref -> "${GitRef.REMOTE_ORIGIN}/$ref" }
    } else {
        repository.fullBranch
    }
}

internal fun String?.shortName(): Result<String> {
    return this?.let {
        when {
            it.startsWith(GitRef.REF_HEAD) -> {
                runCatching {
                    Result.success(it.substringAfter("${GitRef.REF_HEAD}/"))
                }.getOrElse { ex ->
                    Result.failure(GitException(ex))
                }
            }

            it.startsWith(GitRef.REMOTE_ORIGIN) -> {
                runCatching {
                    Result.success(it.substringAfter("${GitRef.REMOTE_ORIGIN}/"))
                }.getOrElse { ex ->
                    Result.failure(GitException(ex))
                }
            }

            else -> Result.failure(UnexpectedException("Unable to parse branch ref: $it"))
        }
    } ?: Result.failure(UnexpectedException("Unable to parse null branch ref"))
}

internal fun Git.calculateBaseBranchVersion(
    targetBranch: GitRef.Branch,
    currentBranch: GitRef.Branch,
    tags: Map<ObjectId, SemVer>
): Result<SemVer?> {
    return headRevInBranch(currentBranch).map { head ->
        findYoungestTagOnBranchOlderThanTarget(targetBranch, head, tags)
    }
}

internal fun Git.headRevInBranch(branch: GitRef.Branch): Result<RevCommit> {
    return runCatching {
        with(repository) {
            RevWalk(this).use { walk ->
                Result.success(
                    walk.parseCommit(this.findRef(branch.refName).objectId).also {
                        walk.dispose()
                    }
                )
            }
        }
    }.getOrElse { ex ->
        log.semverError("Something failed", ex)
        Result.failure(GitException(ex))
    }
}

private fun Git.findYoungestTagOnBranchOlderThanTarget(
    branch: GitRef.Branch,
    target: RevCommit,
    tags: Map<ObjectId, SemVer>
): SemVer? {
    val branchRef = repository.exactRef(branch.refName)
    if (branchRef == null) {
        log.semverError("Failed to find exact git ref for branch: $branch, aborting build. Check that the full git history is available.")
    } else {
        log.semverInfo("Pulling log for $branch refName, exactRef: $branchRef, target: $target")
    }

    return log().add(branchRef.objectId).call()
        .firstOrNull { it.commitTime <= target.commitTime && tags.containsKey(it.toObjectId()) }
        ?.let { tags[it.id] }
}

internal fun gitCommitsSinceBranchPoint(
    git: Git,
    branchPoint: RevCommit,
    branch: GitRef.Branch,
    tags: Map<ObjectId, SemVer>
): Result<Int> {
    val commits = git.log().call().toList()
    val newCommits = commits.takeWhile {
        it.toObjectId() != branchPoint.toObjectId() && it.commitTime > branchPoint.commitTime
    }

    return when {
        newCommits.map { it.toObjectId() }.contains(branchPoint.toObjectId()) -> {
            Result.success(newCommits.size)
        }

        newCommits.size != commits.size -> {
            log.semverInfo(
                buildString {
                    append("Unable to find branch point [${branchPoint.id.name}: ${branchPoint.shortMessage}] ")
                    append("typically this happens when commits were squashed & merged and this branch [$branch] has ")
                    append("not been rebased yet, using nearest commit with a semver tag, this is just an estimate")
                }
            )

            git.findYoungestTagCommitOnBranch(branch, tags)
                ?.let { youngestTag ->
                    log.semverInfo("Youngest tag on this branch is at ${youngestTag.id.name} => ${tags[youngestTag.id]}")
                    Result.success(commits.takeWhile { it.id != youngestTag.id }.size)
                }
                ?: run {
                    log.semverWarn(
                        buildString {
                            append("Failed to find any semver tags on branch [$branch], does main have ")
                            append("any version tags? Using 0 as commit count since branch point")
                        }
                    )
                    Result.success(0)
                }
        }

        else -> {
            Result.failure(
                UnexpectedException(
                    buildString {
                        append("The branch ${branch.refName} did not contain the branch point ")
                        append("[${branchPoint.toObjectId()}: ${branchPoint.shortMessage}], ")
                        append("have you rebased your current branch?")
                    }
                )
            )
        }
    }
}

private fun Git.findYoungestTagCommitOnBranch(
    branch: GitRef.Branch,
    tags: Map<ObjectId, SemVer>,
): RevCommit? {
    val branchRef = repository.exactRef(branch.refName)
    if (branchRef == null) {
        log.semverError("Failed to find exact git ref for branch: $branch, aborting build. Check that the full git history is available.")
    } else {
        log.semverInfo("Pulling log for $branch refName, exactRef: $branchRef")
    }
    return log().add(branchRef.objectId).call()
        .firstOrNull { tags.containsKey(it.toObjectId()) }
}

internal fun Git.hasBranch(shortName: String): List<Ref> {
    return runCatching {
        branchList().setContains(shortName).call().filter {
            it.name.shortName().getOrThrow() == shortName
        }
    }.getOrElse {
        emptyList()
    }
}
