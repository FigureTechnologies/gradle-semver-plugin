package io.github.nefilim.gradle.semver

import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

internal val Project.hasSemVerPlugin: Boolean
    get() = pluginManager.hasPlugin("io.github.nefilim.gradle.semver-plugin")

internal val Project.appliedOnlyOnRootProject: Boolean
    get() = rootProject.hasSemVerPlugin && rootProject.subprojects.none(Project::hasSemVerPlugin)

private val LogPrefix = "semver: ".bold()

internal fun SemVerPluginContext.log(message: String) {
    Logging.getLogger(Logger.ROOT_LOGGER_NAME).lifecycle("$LogPrefix $message")
}
internal fun SemVerPluginContext.warn(message: String) {
    Logging.getLogger(Logger.ROOT_LOGGER_NAME).lifecycle("$LogPrefix ${message.yellow()}")
}
internal fun SemVerPluginContext.error(message: String) {
    Logging.getLogger(Logger.ROOT_LOGGER_NAME).error("$LogPrefix ${message.red()}")
}

internal fun SemVerPluginContext.verbose(message: String) {
    if (config.verbose)
        log(message.purple())
}

private fun String.coloured(c: String) = "$c$this\u001B[0m"
internal fun String.green() = this.coloured("\u001B[32m")
internal fun String.red() = this.coloured("\u001B[31m")
internal fun String.purple() = this.coloured("\u001B[35m")
internal fun String.yellow() = this.coloured("\u001B[33m")
internal fun String.bold() = this.coloured("\u001B[1m")