package com.figure.gradle.semver

import com.figure.gradle.semver.domain.SemVerError
import org.gradle.api.Project
import org.gradle.api.logging.Logger

internal val Project.hasSemVerPlugin: Boolean
    get() = pluginManager.hasPlugin("com.figure.gradle.semver-plugin")

internal val Project.appliedOnlyOnRootProject: Boolean
    get() = rootProject.hasSemVerPlugin && rootProject.subprojects.none(Project::hasSemVerPlugin)

class GradleSemVerContext(private val project: Project, override val ops: ContextProviderOperations): SemVerContext {
    override fun property(name: String): Any? {
        return project.findProperty(name)
    }

    override fun env(name: String): String? {
        return System.getenv(name)
    }
}

private val LogPrefix = "semver: ".bold()

private fun String.coloured(c: String) = "$c$this\u001B[0m"
internal fun String.green() = this.coloured("\u001B[32m")
internal fun String.red() = this.coloured("\u001B[31m")
internal fun String.purple() = this.coloured("\u001B[35m")
internal fun String.yellow() = this.coloured("\u001B[33m")
internal fun String.bold() = this.coloured("\u001B[1m")

internal fun Logger.semver(message: String) = this.lifecycle("semver: ".bold() + message.purple())
internal fun Logger.semverWarn(message: String) = this.lifecycle("semver: ".bold() + message.yellow())
internal fun Logger.semverError(message: String) = this.lifecycle("semver: ".bold() + message.red())