package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.flattenOption
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.core.some
import arrow.core.toOption
import com.javiersc.semver.Version
import io.github.nefilim.gradle.semver.config.PluginConfig
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project

internal fun String?.semverTag(prefix: String): Option<Version> {
    return this?.substringAfterLast("/$prefix")?.let {
        Version.safe(it).fold({ it.some() }, { None })
    } ?: None
}

internal fun Ref?.semverTag(prefix: String): Option<Version> {
    return this?.name?.semverTag(prefix) ?: None
}

internal fun Ref?.shortName(): Either<SemVerError, String> {
    return this?.let {
        when {
            it.name.startsWith(GitRef.RefHead) -> Either.catch { name.substringAfter("${GitRef.RefHead}/") }.mapLeft { SemVerError.Git(it) }
            it.name.startsWith(GitRef.RemoteOrigin) -> Either.catch { name.substringAfter("${GitRef.RemoteOrigin}/") }.mapLeft { SemVerError.Git(it) }
            else -> SemVerError.Unexpected("unable to parse branch Ref: [$it]").left()
        }
    } ?: SemVerError.Unexpected("unable to parse null branch Ref").left()
}

private fun Git.buildRef(refName: String): Either<SemVerError, Ref> {
    return Either.catch { repository.findRef(refName).toOption() }
        .mapLeft { SemVerError.Git(it) }
        .flatMap { it.toEither { SemVerError.MissingRef("could not find a git ref for [$refName]")  } }
}

internal fun Git.tagMap(prefix: String): Map<ObjectId, Version> {
    val versionTags = tagList().call().toList().map { ref ->
        // have to unpeel annotated tags
        ref.semverTag(prefix).map { (repository.refDatabase.peel(ref).peeledObjectId ?: ref.objectId) to it }
    }.flattenOption()
    return versionTags.toMap()
}

internal fun Git.currentVersion(config: PluginConfig, branchRefName: String): Option<Version> {
    val tags = tagMap(config.tagPrefix)
    val tagsIDs = tagMap(config.tagPrefix).keys
    return RevWalk(repository).use { walk ->
        val head = walk.parseCommit(GitRef.Branch.headCommitID(repository, branchRefName))

        walk.markStart(head)
        (walk.firstOrNull() {
            tagsIDs.contains(it.toObjectId()) &&
            tags.containsKey(it.toObjectId())
        }?.let { tags[it.toObjectId()].toOption() } ?: None).also {
            walk.dispose()
        }
    }
}

internal fun SemVerPluginContext.buildBranch(branchRefName: String, config: PluginConfig): Either<SemVerError, GitRef.Branch> {
    return either.eager {
        val shortName = git.buildRef(branchRefName).flatMap { it.shortName() }.bind()
        with (shortName) {
            when {
                equals("main") -> {
                    GitRef.MainBranch(
                        branchRefName,
                        git.currentVersion(config, branchRefName),
                        config.currentBranchScope.getOrElse { GitRef.MainBranch.DefaultScope },
                        config.currentBranchStage.getOrElse { GitRef.MainBranch.DefaultStage }
                    ).right()
                }
                equals("develop") -> {
                    GitRef.DevelopBranch(
                        branchRefName,
                        config.currentBranchScope.getOrElse { GitRef.DevelopBranch.DefaultScope },
                        config.currentBranchStage.getOrElse { GitRef.DevelopBranch.DefaultStage }
                    ).right()
                }
                config.featureBranchRegexes.any { it.matches(shortName) } -> {
                    GitRef.FeatureBranch(
                        shortName,
                        branchRefName,
                        config.currentBranchScope.getOrElse { GitRef.FeatureBranch.DefaultScope },
                        config.currentBranchStage.getOrElse { GitRef.FeatureBranch.DefaultStage }
                    ).right()
                }
                startsWith("hotfix/") -> {
                    GitRef.HotfixBranch(
                        shortName,
                        branchRefName,
                        config.currentBranchScope.getOrElse { GitRef.HotfixBranch.DefaultScope },
                        config.currentBranchStage.getOrElse { GitRef.HotfixBranch.DefaultStage }
                    ).right()
                }
                else -> SemVerError.UnsupportedBranch("unable to determine branch type for branch: $this").left()
            }.bind()
        }
    }
}

internal fun SemVerPluginContext.commitsSinceBranchPoint(
    branchPoint: RevCommit,
    branch: GitRef.Branch,
    tags: Map<ObjectId, Version>,
): Either<SemVerError, Int> {
    val commits = git.log().call().toList() // can this blow up for large repos?
    val newCommits = commits.takeWhile {
        it.toObjectId() != branchPoint.toObjectId() && it.commitTime > branchPoint.commitTime
    }
    return when {
        (newCommits.map { it.toObjectId() }.contains(branchPoint.toObjectId())) -> newCommits.size.right()
        newCommits.size != commits.size -> {
            // find latest tag on this branch
            warn("Unable to find the branch point [${branchPoint.id.name}: ${branchPoint.shortMessage}], typically happens when commits were squashed & merged and this branch [$branch] " +
                    "has not been rebased yet, using nearest commit with a semver tag, this is just a version estimation")
            git.findYoungestTagCommitOnBranch(branch, tags).map { youngestTag ->
                verbose("youngest tag on this branch is at ${youngestTag.id.name} => ${tags[youngestTag.id]}")
                commits.takeWhile { it.id != youngestTag.id }.size
            }.toEither { SemVerError.Unexpected("failed to find any semver tags on branch [$branch], does main have any version tags?") }
        }
        else -> SemVerError.Unexpected("the branch ${branch.refName} did not contain the branch point [${branchPoint.toObjectId()}: ${branchPoint.shortMessage}], have you rebased your current branch?").left()
    }
}

internal fun Git.calculateDevelopBranchVersion(
    main: GitRef.MainBranch,
    develop: GitRef.DevelopBranch,
    tags: Map<ObjectId, Version>,
): Either<SemVerError, Option<Version>> {
    return either.eager {
        val head = headRevInBranch(develop).bind()
        findYoungestTagOnBranchOlderThanTarget(main, head, tags).fold({
            None.right()
        }, {
            applyScopeToVersion(it, develop.scope, develop.stage).map { it.some() }
        }).bind()
    }
}

internal fun Git.headRevInBranch(branch: GitRef.Branch): Either<SemVerError, RevCommit> {
    return Either.catch {
        with(repository) {
            RevWalk(this).use { walk ->
                walk.parseCommit(this.findRef(branch.refName).objectId).also {
                    walk.dispose()
                }
            }
        }
    }.mapLeft { SemVerError.Git(it) }
}

internal fun Git.findYoungestTagOnBranchOlderThanTarget(
    branch: GitRef.Branch,
    target: RevCommit,
    tags: Map<ObjectId, Version>
): Option<Version> {
    return log().add(repository.exactRef(branch.refName).objectId).call()
        .firstOrNull { it.commitTime <= target.commitTime && tags.containsKey(it.toObjectId()) }
        .toOption()
        .flatMap { tags[it.id].toOption() }
}

internal fun Git.findYoungestTagCommitOnBranch(
    branch: GitRef.Branch,
    tags: Map<ObjectId, Version>
): Option<RevCommit> {
    return log().add(repository.exactRef(branch.refName).objectId).call()
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

internal val Project.hasGit: Boolean
    get() = file("${rootProject.rootDir}/.git").exists()

internal val Project.git: Git
    get() =
        Git(
            FileRepositoryBuilder()
                .setGitDir(file("${rootProject.rootDir}/.git"))
                .readEnvironment()
                .findGitDir()
                .build()
        )