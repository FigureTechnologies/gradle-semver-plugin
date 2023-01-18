/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v0

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.continuations.either
import arrow.core.flatMap
import arrow.core.flattenOption
import arrow.core.left
import arrow.core.right
import arrow.core.some
import arrow.core.toOption
import com.figure.gradle.semver.v0.domain.GitRef
import com.figure.gradle.semver.v0.domain.SemverError
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

private val logger = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

typealias Tags = Map<ObjectId, SemVer>

fun getGitContextProviderOperations(git: Git, config: VersionCalculatorConfig): ContextProviderOperations {
    return object : ContextProviderOperations {
        private val tags = git.tagMap(config.tagPrefix)

        override fun currentBranch(): Option<GitRef.Branch> {
            return git.currentBranchRef().flatMap { ref ->
                ref.shortName().map {
                    GitRef.Branch(it, ref)
                }.orNone()
            }
        }

        override fun branchVersion(
            currentBranch: GitRef.Branch,
            targetBranch: GitRef.Branch,
        ): Either<SemverError, Option<SemVer>> {
            return git.calculateBaseBranchVersion(targetBranch, currentBranch, tags)
        }

        override fun commitsSinceBranchPoint(
            currentBranch: GitRef.Branch,
            targetBranch: GitRef.Branch,
        ): Either<SemverError, Int> {
            return either.eager {
                val branchPoint = git.headRevInBranch(currentBranch).bind()
                commitsSinceBranchPoint(git, branchPoint, targetBranch, tags).bind()
            }
        }
    }
}

internal fun Git.currentBranchRef(): Option<String> {
    return if (githubActionsBuild() && pullRequestEvent()) {
        pullRequestHeadRef().map { "${GitRef.RemoteOrigin}/$it" } // why does GITHUB_HEAD_REF not refer to a ref like GITHUB_REF?
    } else
        repository.fullBranch.some()
}

internal fun String?.semverTag(prefix: String): Option<SemVer> {
    return this?.substringAfterLast("/$prefix")?.let {
        if (it.isNotBlank() && it.count { it == '.' } == 2)
            Either.catch { SemVer.parse(it) }.fold({ None }, { it.some() })
        else
            None
    } ?: None
}

internal fun Ref?.semverTag(prefix: String): Option<SemVer> {
    return this?.name?.semverTag(prefix) ?: None
}

internal fun String?.shortName(): Either<SemverError, String> {
    return this?.let {
        when {
            it.startsWith(GitRef.RefHead) -> Either.catch { it.substringAfter("${GitRef.RefHead}/") }
                .mapLeft { SemverError.Git(it) }

            it.startsWith(GitRef.RemoteOrigin) -> Either.catch { it.substringAfter("${GitRef.RemoteOrigin}/") }
                .mapLeft { SemverError.Git(it) }

            else -> SemverError.Unexpected("unable to parse branch Ref: [$it]").left()
        }
    } ?: SemverError.Unexpected("unable to parse null branch Ref").left()
}

internal fun Ref?.shortName(): Either<SemverError, String> {
    return this?.name.shortName()
}

internal fun Git.buildRef(refName: String): Either<SemverError, Ref> {
    return Either.catch { repository.findRef(refName).toOption() }
        .mapLeft { SemverError.Git(it) }
        .flatMap { it.toEither { SemverError.MissingRef("could not find a git ref for [$refName]") } }
}

internal fun Git.hasBranch(shortName: String): List<Ref> {
    return try {
        branchList().setContains(shortName).call().filter {
            it.name.shortName().exists { it == shortName }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

internal fun Git.tagMap(prefix: String): Tags {
    val versionTags = tagList().call().toList().map { ref ->
        // have to unpeel annotated tags
        ref.semverTag(prefix).map { (repository.refDatabase.peel(ref).peeledObjectId ?: ref.objectId) to it }
    }.flattenOption()
    return versionTags.toMap()
}

internal fun Git.currentVersion(tags: Tags, branchRefName: String): Option<SemVer> {
    val tagsIDs = tags.keys
    return RevWalk(repository).use { walk ->
        val head = walk.parseCommit(GitRef.Branch.headCommitID(repository, branchRefName))

        walk.markStart(head)
        (walk.firstOrNull() {
            tagsIDs.contains(it.toObjectId()) &&
                    tags.containsKey(it.toObjectId())
        }?.let {
            tags[it.toObjectId()].toOption()
        } ?: None).also {
            walk.dispose()
        }
    }
}

internal fun commitsSinceBranchPoint(
    git: Git,
    branchPoint: RevCommit,
    branch: GitRef.Branch,
    tags: Tags,
): Either<SemverError, Int> {
    val commits = git.log().call().toList() // can this blow up for large repos?
    val newCommits = commits.takeWhile {
        it.toObjectId() != branchPoint.toObjectId() && it.commitTime > branchPoint.commitTime
    }
    return when {
        (newCommits.map { it.toObjectId() }.contains(branchPoint.toObjectId())) -> newCommits.size.right()
        newCommits.size != commits.size -> {
            // find latest tag on this branch
            logger.semverWarn("Unable to find the branch point [${branchPoint.id.name}: ${branchPoint.shortMessage}] typically happens when commits were squashed & merged and this branch [$branch] has not been rebased yet, using nearest commit with a semver tag, this is just a version estimation".yellow())
            git.findYoungestTagCommitOnBranch(branch, tags).fold({
                logger.semverWarn("failed to find any semver tags on branch [$branch], does main have any version tags? using 0 as commit count since branch point")
                0
            }, { youngestTag ->
                logger.info("youngest tag on this branch is at ${youngestTag.id.name} => ${tags[youngestTag.id]}")
                commits.takeWhile { it.id != youngestTag.id }.size
            }).right()
        }

        else -> SemverError.Unexpected("the branch ${branch.refName} did not contain the branch point [${branchPoint.toObjectId()}: ${branchPoint.shortMessage}], have you rebased your current branch?")
            .left()
    }
}

internal fun Git.calculateBaseBranchVersion(
    targetBranch: GitRef.Branch,
    currentBranch: GitRef.Branch,
    tags: Tags,
): Either<SemverError, Option<SemVer>> {
    return headRevInBranch(currentBranch).map { head ->
        findYoungestTagOnBranchOlderThanTarget(targetBranch, head, tags)
    }
}

internal fun Git.headRevInBranch(branch: GitRef.Branch): Either<SemverError, RevCommit> {
    return Either.catch {
        with(repository) {
            RevWalk(this).use { walk ->
                walk.parseCommit(this.findRef(branch.refName).objectId).also {
                    walk.dispose()
                }
            }
        }
    }.mapLeft { SemverError.Git(it) }
}

internal fun Git.findYoungestTagOnBranchOlderThanTarget(
    branch: GitRef.Branch,
    target: RevCommit,
    tags: Tags,
): Option<SemVer> {
    val branchRef = repository.exactRef(branch.refName)
    if (branchRef == null)
        logger.semverError("failed to find exact git ref for branch [$branch], aborting...")
    else
        logger.info("pulling log for $branch refName, exactRef: ${branchRef}, target: $target")
    return log().add(branchRef.objectId).call()
        .firstOrNull { it.commitTime <= target.commitTime && tags.containsKey(it.toObjectId()) }
        .toOption()
        .flatMap { tags[it.id].toOption() }
}

internal fun Git.findYoungestTagCommitOnBranch(
    branch: GitRef.Branch,
    tags: Tags,
): Option<RevCommit> {
    val branchRef = repository.exactRef(branch.refName)
    if (branchRef == null)
        logger.semverError("failed to find exact git ref for branch [$branch], aborting...")
    else
        logger.info("pulling log for $branch refName, exactRef: $branchRef")
    return log().add(branchRef.objectId).call()
        .firstOrNull { tags.containsKey(it.toObjectId()) }
        .toOption()
}

internal fun Git.hasCommits(): Boolean {
    return try {
        log().call().toList().isNotEmpty()
    } catch (e: Exception) {
        false
    }
}

internal fun Project.hasGit(gitDir: String): Boolean =
    file(gitDir).exists()

internal fun Project.git(gitDir: String): Git =
    Git(
        FileRepositoryBuilder()
            .setGitDir(file(gitDir))
            .readEnvironment()
            .findGitDir()
            .build()
    )
