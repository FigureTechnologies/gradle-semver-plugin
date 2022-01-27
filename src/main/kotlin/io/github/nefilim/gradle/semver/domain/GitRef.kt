package io.github.nefilim.gradle.semver.domain

import arrow.core.Option
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.Stage
import net.swiftzer.semver.SemVer
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository

sealed interface GitRef {

    companion object {
        val RefHead = Constants.R_HEADS.substringBeforeLast("/")
        val RefRemote = Constants.R_REMOTES.substringBeforeLast("/")
        val RemoteOrigin = "$RefRemote/${Constants.DEFAULT_REMOTE_NAME}"
    }

    sealed interface Branch {
        val name: String // example: `main`
        val refName: String // example: `refs/heads/main`
        val scope: Scope
        val stage: Stage
        fun headCommitID(repo: Repository): ObjectId = repo.findRef(refName).objectId

        companion object {
            fun headCommitID(repo: Repository, refName: String): ObjectId = repo.findRef(refName).objectId
        }
    }

    sealed interface VersionableBranch: Branch {
        val version: Option<SemVer>
    }

    data class MainBranch(
        override val name: String = Name,
        override val refName: String = RefName,
        override val version: Option<SemVer>,
        override val scope: Scope = DefaultScope,
        override val stage: Stage = DefaultStage,
    ) : GitRef, VersionableBranch {

        companion object {
            const val Name = "main"
            const val AlternativeName = "master"
            val RefName = "$RefHead/$Name"
            val RemoteOriginRefName = "$RemoteOrigin/$Name"
            val AlternativeRefName = "$RefHead/$AlternativeName"
            val AlternativeRemoteOriginRefName = "$RemoteOrigin/$AlternativeName"
            val PossibleBranchRefs = setOf(RefName, RemoteOriginRefName, AlternativeRefName, AlternativeRemoteOriginRefName)
            val DefaultScope = Scope.Minor
            val DefaultStage = Stage.Final

            fun determineName(refName: String): String {
                return if (refName.contains(AlternativeName, ignoreCase = true))
                    AlternativeName
                else
                    Name
            }
        }
    }

    data class DevelopBranch(
        override val refName: String = RefName,
        override val scope: Scope = DefaultScope,
        override val stage: Stage = DefaultStage,
    ) : GitRef, Branch {
        override val name = Name

        companion object {
            const val Name = "develop"
            val RefName = "$RefHead/$Name"
            val RemoteOriginRefName = "$RemoteOrigin/$Name"
            val DefaultScope = Scope.Patch
            val DefaultStage = Stage.Beta
        }
    }

    data class FeatureBranch(
        override val name: String,
        override val refName: String = "$RefHead/$name",
        override val scope: Scope = DefaultScope,
        override val stage: Stage = DefaultStage,
    ) : GitRef, Branch {
        companion object {
            val DefaultScope = Scope.Patch
            val DefaultStage = Stage.Alpha
            val DefaultRegex = "^feature\\/.+".toRegex()
        }
    }

    data class HotfixBranch(
        override val name: String,
        override val refName: String = "$RefHead/$name",
        override val scope: Scope = FeatureBranch.DefaultScope,
        override val stage: Stage = FeatureBranch.DefaultStage,
    ) : GitRef, Branch {
        init {
            require(name.startsWith("hotfix/", ignoreCase = true))
        }
        companion object {
            val DefaultScope = Scope.Patch
            val DefaultStage = Stage.Beta
        }
    }
}
