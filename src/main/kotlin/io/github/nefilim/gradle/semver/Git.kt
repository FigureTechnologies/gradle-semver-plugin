package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.flattenOption
import arrow.core.left
import arrow.core.right
import arrow.core.some
import arrow.core.toOption
import com.javiersc.semver.Version
import io.github.nefilim.gradle.semver.config.PluginConfig
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
    return this?.let { Either.catch { name.substringAfterLast('/') }.mapLeft { SemVerError.Git(it) } } ?: SemVerError.Unexpected("empty git ref, unable to find shortname").left()
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

internal fun Git.currentMainVersion(config: PluginConfig): Option<Version> {
    val tags = tagMap(config.tagPrefix)
    val tagsIDs = tagMap(config.tagPrefix).keys
    return RevWalk(repository).use { walk ->
        val head = walk.parseCommit(GitRef.MainBranch.headCommitID(repository))

        walk.markStart(head)
        (walk.firstOrNull() {
            tagsIDs.contains(it.toObjectId()) &&
            tags.containsKey(it.toObjectId())
        }?.let { tags[it.toObjectId()].toOption() } ?: None).also {
            walk.dispose()
        }
    }
}

internal fun Git.buildBranch(branchRefName: String, config: PluginConfig): Either<SemVerError, GitRef.Branch> {
    return either.eager {
        val shortName = buildRef(branchRefName).flatMap { it.shortName() }.bind()
        with (shortName) {
            when {
                equals("main") -> GitRef.MainBranch(branchRefName, currentMainVersion(config), config.mainScope, config.mainStage).right()
                equals("develop") -> GitRef.DevelopBranch(branchRefName, config.developScope, config.developStage).right()
                startsWith("feature/") -> GitRef.FeatureBranch(shortName, branchRefName, config.featureScope, config.featureStage).right()
                startsWith("hotfix/") -> GitRef.HotfixBranch(shortName, branchRefName, config.hotfixScope, config.hotfixStage).right()
                else -> SemVerError.UnsupportedBranch("unable to determine branch type for branch: $this").left()
            }.bind()
        }
    }
}

internal fun Git.hasCommits(): Boolean {
    return try {
        log().call().toList().isNotEmpty()
    } catch (e: Exception) {
        false
    }
}

internal fun Git.commitsSinceBranchPoint(branchPoint: RevCommit, branch: GitRef.Branch): Either<SemVerError, Int> {
    val commits = log().call().toList() // can this blow up for large repos?
    return commits.takeWhile {
        it.toObjectId() != branchPoint.toObjectId()
    }.size.let {
        if (it == commits.size)
            SemVerError.Unexpected("the branch ${branch.refName} did not contain the branch point $branchPoint, have you rebased your current branch?").left()
        else
            it.right()
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