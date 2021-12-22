package io.github.nefilim.gradle.semver

import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import org.gradle.api.Project
import java.io.File

internal val Project.hasSemVerPlugin: Boolean
    get() = pluginManager.hasPlugin("io.github.nefilim.gradle.semver-plugin")

internal val Project.appliedOnlyOnRootProject: Boolean
    get() = rootProject.hasSemVerPlugin && rootProject.subprojects.none(Project::hasSemVerPlugin)

internal fun SemVerPluginContext.generateVersionFile() {
    with (project) {
        File("$buildDir/semver/version.txt").apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(
                """
                   |$version
                   |${config.tagPrefix}$version
                   |
                """.trimMargin()
            )
        }
    }
}

internal fun Project.semverMessage(message: Any) = logger.lifecycle("$message")