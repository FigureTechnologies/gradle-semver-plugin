package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.some
import arrow.core.toOption
import io.github.nefilim.gradle.semver.config.PluginConfig
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import com.javiersc.semver.Version
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project

internal fun String?.semverTag(prefix: String): Option<Version> {
    return this?.substringAfterLast("/$prefix")?.let {
        println("name: $this sub: $it")
        Version.safe(it).fold({ it.some() }, { None }).also {
            println("result: $it")
        }
    } ?: None
}

internal fun Ref?.semverTag(prefix: String): Option<Version> {
    return this?.name?.semverTag(prefix) ?: None
}

internal fun Ref?.shortName(): Either<SemVerError, String> {
    return this?.let { Either.catch { name.substringAfterLast('/') }.mapLeft { SemVerError.Git(it) } } ?: SemVerError.Unexpected("empty git ref, unable to find shortname").left()
}

private fun Git.buildRef(refName: String): Either<SemVerError, Ref> = Either.catch { repository.findRef(refName) }.mapLeft { SemVerError.Git(it) }

internal fun Git.tagMap(): Map<ObjectId, Ref> = tagList().call().associateBy { it.objectId }

internal fun Git.currentMainVersion(config: PluginConfig): Option<Version> {
    val tags = tagMap()
    val tagsIDs = tagMap().keys
    return RevWalk(repository).use { walk ->
        val head = walk.parseCommit(GitRef.MainBranch.headCommitID(repository))

        walk.markStart(head)
        (walk.firstOrNull() {
            println("does $tagsIDs contain? ${it.toObjectId()}: ${tagsIDs.contains(it.toObjectId())}")
            tagsIDs.contains(it.toObjectId()) &&
            tags[it.toObjectId()].semverTag(config.tagPrefix).isDefined()
        }?.let { tags[it.toObjectId()].semverTag(config.tagPrefix) } ?: None).also {
            walk.dispose()
        }
    }
}

internal fun Git.buildBranch(branchRefName: String, config: PluginConfig): Either<SemVerError, GitRef.Branch> {
    return either.eager {
        val shortName = buildRef(branchRefName).flatMap { it.shortName() }.bind()
        with (shortName) {
            when {
                equals("main") -> io.github.nefilim.gradle.semver.domain.GitRef.MainBranch(currentMainVersion(config)).right()
                equals("develop") -> io.github.nefilim.gradle.semver.domain.GitRef.DevelopBranch().right()
                startsWith("feature/") -> io.github.nefilim.gradle.semver.domain.GitRef.FeatureBranch(shortName, branchRefName).right()
                startsWith("hotfix/") -> io.github.nefilim.gradle.semver.domain.GitRef.HotfixBranch(shortName, branchRefName).right()
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
            SemVerError.Unexpected("the branch ${branch.refName} did not contain the branch point $branchPoint!?").left()
        else
            it.right()
    }
}

internal fun Repository.calculateDevelopBranchVersion(
    config: PluginConfig,
    main: GitRef.MainBranch,
    develop: GitRef.DevelopBranch,
    tags: Map<ObjectId, Ref>,
): Either<SemVerError, Option<Version>> {
    return either.eager {
        val head = headRevInBranch(develop).bind()

        findYoungestTagOnBranchOlderThanTarget(config, main, head, tags).fold({
            None.right()
        }, {
            applyScopeToVersion(it, develop.scope, develop.stage).map { it.some() }
        }).bind()
    }
}

internal fun Repository.headRevInBranch(branch: GitRef.Branch): Either<SemVerError, RevCommit> {
    return Either.catch {
        RevWalk(this).use { walk ->
            walk.parseCommit(this.findRef(branch.refName).objectId).also {
                walk.dispose()
            }
        }
    }.mapLeft { SemVerError.Git(it) }
}

internal fun Repository.findYoungestTagOnBranchOlderThanTarget(
    config: PluginConfig,
    branch: GitRef.Branch,
    target: RevCommit,
    tags: Map<ObjectId, Ref>
): Option<Version> {
    return RevWalk(this).use { walk ->
        val head = walk.parseCommit(this.findRef(branch.refName).objectId)

        walk.markStart(head)
        walk.first { it.commitTime <= target.commitTime }
            .toOption()
            .flatMap { tags[it.id].semverTag(config.tagPrefix) }
            .also {
                walk.dispose()
            }
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

//internal val Git.headRef: Ref
//    get() = repository.findRef(repository.fullBranch)
//


// branches
//internal val Git.currentBranch: GitRef.Branch
//    get() =
//        repository.run {
//            GitRef.Branch(branch, fullBranch, commitsInCurrentBranch, tagsInCurrentBranch)
//        }

//// commits
//internal val Git.headCommit: GitRef.Head
//    get() =
//        GitRef.Head(
//            GitRef.Commit(
//                message = headRevCommit.shortMessage,
//                fullMessage = headRevCommit.fullMessage,
//                hash = headRevCommit.toObjectId().name,
//            )
//        )
//
//internal val Git.headRevCommit: RevCommit
//    get() = RevWalk(repository).parseCommit(repository.resolve(Constants.HEAD))
//
//internal val Git.headRevCommitInBranch: RevCommit
//    get() = RevWalk(repository).parseCommit(headRef.objectId)

/*internal val Git.lastCommitInCurrentBranch: GitRef.Commit?
    get() = commitsInCurrentBranch.firstOrNull()

internal val Git.commitsInCurrentBranchRevCommit: List<RevCommit>
    get() = log().call().toList()

internal val Git.commitsInCurrentBranch: List<GitRef.Commit>
    get() =
        commitsInCurrentBranchRevCommit.map { revCommit ->
            revCommit.run {
                GitRef.Commit(
                    message = shortMessage,
                    fullMessage = fullMessage,
                    hash = toObjectId().name,
                )
            }
        }

internal val Git.commitsInCurrentBranchHash: List<String>
    get() = commitsInCurrentBranchRevCommit.map(RevCommit::getName)

internal fun Git.commitHash(ref: Ref): String = commitHash(ref.objectId)

internal fun Git.commitHash(objectId: ObjectId): String =
    repository.parseCommit(objectId).toObjectId().name

internal fun Git.commitsBetweenTwoCommitsIncludingLastExcludingFirst(
    fromCommit: GitRef.Commit?,
    toCommit: GitRef.Commit?,
): List<GitRef.Commit> {
    val to = commitsInCurrentBranch.indexOf(toCommit)
    val from = commitsInCurrentBranch.indexOf(fromCommit)

    return when {
        (to == -1 || from == -1) -> emptyList()
        to > from -> commitsInCurrentBranch.subList(from, to)
        else -> commitsInCurrentBranch.subList(to, from)
    }
}

internal val Git.commitsInCurrentBranchFullMessage: List<String>
    get() = commitsInCurrentBranchRevCommit.map(RevCommit::getFullMessage)

internal fun Git.lastVersionCommitInCurrentBranch(tagPrefix: String): GitRef.Commit? =
    lastVersionTagInCurrentBranch(tagPrefix)?.commit

// tags
internal val Ref.tagName: String
    get() = name.substringAfter("refs/tags/")

internal val Git.tagsInRepo: List<GitRef.Tag>
    get() =
        tagsInRepoRef.map { ref ->
            val commit = repository.parseCommit(ref.objectId)
            GitRef.Tag(
                name = ref.tagName,
                refName = ref.name,
                commit =
                    GitRef.Commit(
                        message = commit.shortMessage,
                        fullMessage = commit.fullMessage,
                        hash = commit.toObjectId().name,
                    )
            )
        }

internal val Git.tagsInRepoRef: List<Ref>
    get() = Git(repository).tagList().call()

internal val Git.tagsInRepoHash: List<String>
    get() = tagsInRepoRef.map(::commitHash)

internal val Git.tagsInRepoName: List<String>
    get() = tagsInRepoRef.map(Ref::getName)

internal val Git.tagsInCurrentBranch: List<GitRef.Tag>
    get() =
        tagsInCurrentBranchRef.map { ref ->
            val commit = repository.parseCommit(ref.objectId)
            GitRef.Tag(
                name = ref.tagName,
                refName = ref.name,
                commit =
                    GitRef.Commit(
                        message = commit.shortMessage,
                        fullMessage = commit.fullMessage,
                        hash = commit.toObjectId().name,
                    )
            )
        }

internal val Git.tagsInCurrentBranchRef: List<Ref>
    get() = tagsInRepoRef.filter { ref -> commitHash(ref) in commitsInCurrentBranchHash }

internal val Git.tagsInCurrentBranchHash: List<String>
    get() = tagsInCurrentBranchRef.map(::commitHash)

internal val Git.tagsInCurrentBranchName: List<String>
    get() = tagsInCurrentBranchRef.map(Ref::getName)

internal fun Git.tagsInCurrentCommit(hash: String): List<GitRef.Tag> =
    tagsInCurrentBranch.filter { it.commit.hash == hash }

internal fun Git.isThereVersionTag(tagPrefix: String): Boolean =
    versionTagsInCurrentBranch(tagPrefix).isNotEmpty()

internal fun Git.versionTagsInCurrentCommit(hash: String, tagPrefix: String): List<GitRef.Tag> =
    tagsInCurrentCommit(hash).filter { tag ->
        tag.name.startsWith(tagPrefix) && Version.safe(tag.name.removePrefix(tagPrefix)).isSuccess
    }

internal fun Git.versionTagsInCurrentBranch(tagPrefix: String): List<GitRef.Tag> =
    tagsInCurrentBranch.filter { tag ->
        tag.name.startsWith(tagPrefix) && Version.safe(tag.name.removePrefix(tagPrefix)).isSuccess
    }

internal fun Git.versionsInCurrentBranch(tagPrefix: String): List<Version> =
    versionTagsInCurrentBranch(tagPrefix).mapNotNull { tag ->
        Version.safe(tag.name.removePrefix(tagPrefix)).getOrNull()
    }

internal fun Git.versionTagsSortedBySemver(tagPrefix: String): List<GitRef.Tag> =
    versionTagsInCurrentBranch(tagPrefix).sortedBy { tag ->
        Version.safe(tag.name.removePrefix(tagPrefix)).getOrNull()
    }

internal fun Git.versionTagsInCurrentBranchSortedByTimelineOrSemverOrder(
    tagPrefix: String
): List<GitRef.Tag> {
    val commitsByHash: Map<String, Int> =
        commitsInCurrentBranchHash.withIndex().associate { it.value to it.index }

    return versionTagsSortedBySemver(tagPrefix).sortedByDescending { tag ->
        commitsByHash[tag.commit.hash]
    }
}

internal fun Git.lastVersionTagInCurrentBranch(tagPrefix: String): GitRef.Tag? =
    versionTagsInCurrentBranchSortedByTimelineOrSemverOrder(tagPrefix).lastOrNull()

// versions
internal fun Git.lastVersionInCurrentBranch(
    warningLastVersionIsNotHigherVersion: (last: Version?, higher: Version?) -> Unit,
    tagPrefix: String,
): Version =
    versionTagsInCurrentCommit(headCommit.commit.hash, tagPrefix).lastResultVersion(tagPrefix)
        ?: lastVersionTagInCurrentBranch(tagPrefix)?.name?.removePrefix(tagPrefix).run {
            if (this != null) {
                val lastVersion: Version? = Version.safe(this).getOrNull()
                val higherVersion: Version? = versionsInCurrentBranch(tagPrefix).firstOrNull()

                if (lastVersion != null && higherVersion != null && higherVersion > lastVersion) {
                    warningLastVersionIsNotHigherVersion(lastVersion, higherVersion)
                }

                lastVersion
            } else null
        }
            ?: InitialVersion

private fun List<GitRef.Tag>.lastResultVersion(tagPrefix: String): Version? =
    asSequence()
        .filter { tag -> tag.name.startsWith(tagPrefix) }
        .map { tag -> tag.name.substringAfter(tagPrefix) }
        .map(Version.Companion::safe)
        .mapNotNull(Result<Version>::getOrNull)
        .toList()
        .run { maxOrNull() }
*/
