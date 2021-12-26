package io.github.nefilim.gradle.semver.config

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import io.github.nefilim.gradle.semver.domain.GitRef
import com.javiersc.semver.Version
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
        private val DefaultVersion = Version(0, 1, 0, null, null)
        private const val DefaultTagPrefix = "v"

        fun fromProjectProperties(project: Project): PluginConfig {
            return with (project) {
                PluginConfig(
                    findValue(SemVerProperties.TagPrefix, DefaultTagPrefix),
                    findValue(SemVerProperties.InitialVersion, DefaultVersion),
                    findValue(SemVerProperties.OverrideVersion, None) { Some(Version(it)) },
                    findValue(SemVerProperties.MainScope, GitRef.MainBranch.DefaultScope),
                    findValue(SemVerProperties.MainStage, GitRef.MainBranch.DefaultStage),
                    findValue(SemVerProperties.DevelopScope, GitRef.DevelopBranch.DefaultScope),
                    findValue(SemVerProperties.DevelopStage, GitRef.DevelopBranch.DefaultStage),
                    findValue(SemVerProperties.FeatureScope, GitRef.FeatureBranch.DefaultScope),
                    findValue(SemVerProperties.FeatureStage, GitRef.FeatureBranch.DefaultStage),
                    findValue(SemVerProperties.HotfixScope, GitRef.HotfixBranch.DefaultScope),
                    findValue(SemVerProperties.HotfixStage, GitRef.HotfixBranch.DefaultStage),
                )
            }
        }

        private fun Project.findValue(key: SemVerProperties, default: String): String = findValue(key, default) { it }
        private fun Project.findValue(key: SemVerProperties, default: Version): Version = findValue(key, default) { Version(it) }
        private fun Project.findValue(key: SemVerProperties, default: Stage): Stage = findValue(key, default) { Stage.fromValue(it, default) }
        private fun Project.findValue(key: SemVerProperties, default: Scope): Scope = findValue(key, default) { Scope.fromValue(it, default) }

        private fun <T>Project.findValue(key: SemVerProperties, default: T, f: (String) -> T): T {
            return (findProperty(key.key) ?: findProperty(key.camelCaseName()))?.let {
                f(it.toString())
            } ?: default
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
    OverrideVersion("semver.overrideVersion"),

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
    CheckClean("semver.checkClean");

    fun camelCaseName(): String {
        return DotSeperatorRegex.replace(this.key) { it.value.trimStart('.').uppercase() }
    }

    companion object {
        val DotSeperatorRegex = "\\.[a-zA-Z]".toRegex()
    }
}

fun main() {
    println("${SemVerProperties.TagPrefix.key} => ${SemVerProperties.TagPrefix.camelCaseName()}")
    println("${SemVerProperties.DevelopStage.key} => ${SemVerProperties.DevelopStage.camelCaseName()}")
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