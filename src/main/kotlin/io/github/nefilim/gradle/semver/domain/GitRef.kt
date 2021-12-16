package io.github.nefilim.gradle.semver.domain

import arrow.core.Option
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.Stage
import com.javiersc.semver.Version
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
        fun headCommitID(repo: Repository): ObjectId = repo.findRef(refName).objectId

        companion object {
            fun headCommitID(repo: Repository, refName: String): ObjectId = repo.findRef(refName).objectId
        }
    }

    sealed interface VersionableBranch: Branch {
        val version: Option<Version>
        val scope: Scope
        val stage: Stage
    }

    data class MainBranch(
        override val refName: String = DevelopBranch.RefName,
        override val version: Option<Version>,
        override val scope: Scope = DefaultScope,
        override val stage: Stage = DefaultStage,
    ) : GitRef, VersionableBranch {
        override val name = Name

        companion object {
            const val Name = "main"
            val RefName = "$RefHead/$Name"
            val RemoteOriginRefName = "$RemoteOrigin/$Name"
            val DefaultScope = Scope.Minor
            val DefaultStage = Stage.Final
        }
    }

    data class DevelopBranch(
        override val refName: String = RefName,
        val scope: Scope = DefaultScope,
        val stage: Stage = DefaultStage,
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
        val scope: Scope = DefaultScope,
        val stage: Stage = DefaultStage,
    ) : GitRef, Branch {
        init {
            require(name.startsWith("feature/", ignoreCase = true))
        }
        companion object {
            val DefaultScope = Scope.Patch
            val DefaultStage = Stage.Alpha
        }
    }

    data class HotfixBranch(
        override val name: String,
        override val refName: String = "$RefHead/$name",
        val scope: Scope = FeatureBranch.DefaultScope,
        val stage: Stage = FeatureBranch.DefaultStage,
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
