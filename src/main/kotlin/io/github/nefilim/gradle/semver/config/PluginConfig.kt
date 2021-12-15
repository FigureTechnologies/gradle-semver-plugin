package io.github.nefilim.gradle.semver.config

import io.github.nefilim.gradle.semver.domain.GitRef
import com.javiersc.semver.Version
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.gradle.api.Project

data class PluginConfig(
    val tagPrefix: String,
    val initialVersion: Version,

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
        private val DefaultVersion = Version(0, 1, 0, null, null)
        private const val DefaultTagPrefix = "v"

        fun fromProjectProperties(project: Project): PluginConfig {
            return with (project) {
                PluginConfig(
                    properties[SemVerProperties.TagPrefix.key]?.toString() ?: DefaultTagPrefix,
                    properties[SemVerProperties.InitialVersion.key]?.toString()?.let { Version(it) } ?: DefaultVersion,
                    Scope.fromValue(properties[SemVerProperties.MainScope.key]?.toString(), GitRef.MainBranch.DefaultScope),
                    Stage.fromValue(properties[SemVerProperties.MainStage.key]?.toString(), GitRef.MainBranch.DefaultStage),
                    Scope.fromValue(properties[SemVerProperties.DevelopScope.key]?.toString(), GitRef.DevelopBranch.DefaultScope),
                    Stage.fromValue(properties[SemVerProperties.DevelopStage.key]?.toString(), GitRef.DevelopBranch.DefaultStage),
                    Scope.fromValue(properties[SemVerProperties.FeatureScope.key]?.toString(), GitRef.FeatureBranch.DefaultScope),
                    Stage.fromValue(properties[SemVerProperties.FeatureStage.key]?.toString(), GitRef.FeatureBranch.DefaultStage),
                    Scope.fromValue(properties[SemVerProperties.HotfixScope.key]?.toString(), GitRef.HotfixBranch.DefaultScope),
                    Stage.fromValue(properties[SemVerProperties.HotfixStage.key]?.toString(), GitRef.HotfixBranch.DefaultStage),
                )
            }
        }
    }
}

data class SemVerPluginContext(
    val git: Git,
    val config: PluginConfig,
    val project: Project,
) {
    val repository: Repository = git.repository
}

internal enum class SemVerProperties(val key: String) {
    TagPrefix("semver.tagPrefix"),
    InitialVersion("semver.initialVersion"),

    MainStage("semver.main.stage"),
    MainScope("semver.main.scope"),
    DevelopStage("semver.develop.stage"),
    DevelopScope("semver.develop.scope"),
    FeatureStage("semver.feature.stage"),
    FeatureScope("semver.feature.scope"),
    HotfixStage("semver.hotfix.stage"),
    HotfixScope("semver.hotfix.scope"),

    MockDate("semver.mockDateOfEpochSecond"),
    Remote("semver.remote"),
    CheckClean("semver.checkClean"),
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