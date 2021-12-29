package io.github.nefilim.gradle.semver.config

import arrow.core.None
import arrow.core.Option
import com.javiersc.semver.Version
import io.github.nefilim.gradle.semver.domain.GitRef
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.gradle.api.Project

data class PluginConfig(
    val tagPrefix: String,
    val initialVersion: Version,
    val overrideVersion: Option<Version> = None,

    val mainScope: Scope = GitRef.MainBranch.DefaultScope,
    val mainStage: Stage = GitRef.MainBranch.DefaultStage,

    val developScope: Scope = GitRef.DevelopBranch.DefaultScope,
    val developStage: Stage = GitRef.DevelopBranch.DefaultStage,

    val featureScope: Scope = GitRef.FeatureBranch.DefaultScope,
    val featureStage: Stage = GitRef.FeatureBranch.DefaultStage,

    val hotfixScope: Scope = GitRef.HotfixBranch.DefaultScope,
    val hotfixStage: Stage = GitRef.HotfixBranch.DefaultStage,
) {
    companion object {
        internal val DefaultVersion = Version(0, 1, 0, null, null)
        internal const val DefaultTagPrefix = "v"
    }
}

data class SemVerPluginContext(
    val git: Git,
    val config: PluginConfig,
    val project: Project,
) {
    val repository: Repository = git.repository
}

enum class Stage(private val value: String) {
    Alpha("alpha"),
    Beta("beta"),
    Auto("auto"),
    Final("final"),
    Snapshot("snapshot");

    operator fun invoke(): String = value

    companion object {
        private val map = values().associateBy { it.value }

        fun fromValue(value: String?, default: Stage): Stage {
            return if (value != null )
                map[value.lowercase()] ?: throw IllegalArgumentException("invalid stage: ${value.lowercase()}, valid values: ${values().joinToString { it.value }}")
            else
                default
        }
    }
}

enum class Scope(private val value: String) {
    Auto("auto"),
    Major("major"),
    Minor("minor"),
    Patch("patch");

    operator fun invoke(): String = value

    companion object {
        private val map = values().associateBy { it.value }

        fun fromValue(value: String?, default: Scope): Scope {
            return if (value != null )
                map[value.lowercase()] ?: throw IllegalArgumentException("invalid scope: ${value.lowercase()}, valid values: ${values().joinToString { it.value }}")
            else
                default
        }
    }
}

fun possiblyPrefixedVersion(version: String, prefix: String): Version {
    return Version(version.trimMargin(prefix)) // fail fast, don't let an invalid version propagate to runtime
}