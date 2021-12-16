package io.github.nefilim.gradle.semver.domain

import arrow.core.Option
import arrow.core.some
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.Stage
import com.javiersc.semver.Version
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository

sealed interface GitRef {

    companion object {
        val RefHead = Constants.R_HEADS.substringBeforeLast("/")
    }

    sealed interface Branch {
        val name: String // example: `main`
        val refName: String // example: `refs/heads/main`
        fun headCommitID(repo: Repository): ObjectId = repo.findRef(refName).objectId
    }

    sealed interface VersionableBranch: Branch {
        val version: Option<Version>
        val scope: Scope
        val stage: Stage
    }

    data class MainBranch(
        override val version: Option<Version>,
        override val scope: Scope = DefaultScope,
        override val stage: Stage = DefaultStage,
    ) : GitRef, VersionableBranch {
        override val name = Name
        override val refName = RefName

        companion object {
            const val Name = "main"
            val RefName = "$RefHead/$Name"
            val DefaultScope = Scope.Minor
            val DefaultStage = Stage.Final
            fun headCommitID(repo: Repository): ObjectId = repo.findRef(RefName).objectId
        }
    }

    data class VersionedDevelopBranch(
        override val version: Option<Version>,
        override val scope: Scope = Scope.Patch,
        override val stage: Stage = MainBranch.DefaultStage,
    ) : GitRef, VersionableBranch {
        override val name = DevelopBranch.Name
        override val refName = DevelopBranch.RefName
    }

    data class DevelopBranch(
        val scope: Scope = DefaultScope,
        val stage: Stage = DefaultStage,
    ) : GitRef, Branch {
        override val name = Name
        override val refName = RefName

        fun toVersionedDevelopBranch(version: Version): VersionedDevelopBranch {
            return VersionedDevelopBranch(
                version.some(),
                scope,
                stage
            )
        }

        companion object {
            const val Name = "develop"
            val RefName = "$RefHead/$Name"
            val DefaultScope = Scope.Patch
            val DefaultStage = Stage.Beta
            fun headCommitID(repo: Repository): ObjectId = repo.findRef(RefName).objectId
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
