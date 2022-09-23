/**
 * Copyright (c) 2022 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver

import com.figure.gradle.semver.SemverExtension.Companion.semver
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import java.io.File
import java.nio.file.Files

open class SemverPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.semver() // impure! but needed to create and register the extension with the project so that we can use it in the tasks below

        if (!target.hasGit) {
            target.logger.warn("the current directory is not part of a git repo, cannot determine project semantic version number, please initialize a git repo with main & develop branches")
        }

        target.tasks.register("cv", CurrentVersionTask::class.java)

        Files.createDirectory(target.buildDir.toPath())

        target.tasks.register("generateVersionFile", GenerateVersionFileTask::class.java) {
            it.buildDir = target.buildDir
            it.targetExtension = target.extensions[SemverExtension.ExtensionName] as SemverExtension
        }

        target.tasks.register("createAndPushVersionTag", CreateAndPushVersionTag::class.java)
    }
}

open class CurrentVersionTask : DefaultTask() {
    @TaskAction
    fun currentVersion() {
        project.logger.lifecycle("version: ${(project.extensions[SemverExtension.ExtensionName] as SemverExtension).version}".purple())
    }
}

abstract class GenerateVersionFileTask : DefaultTask() {

    @get:InputDirectory
    abstract var buildDir: File

    @get:Input
    abstract var targetExtension: SemverExtension

    @TaskAction
    fun generateVersionFile() {
        println("hey hatz $buildDir")

        File("${buildDir}/semver/version.txt").apply {
            this.parentFile.mkdirs()
            this.createNewFile()
            this.writeText(
                """
                   |${targetExtension.version}
                   |${targetExtension.versionTagName}
                """.trimMargin()
            )
        }
    }
}

open class CreateAndPushVersionTag : DefaultTask() {
    @TaskAction
    fun createAndPushTag() {
        val extension = (project.extensions[SemverExtension.ExtensionName] as SemverExtension)
        project.git.tag().setName(extension.versionTagName).call()
        project.logger.semver("created version tag: ${extension.versionTagName}, pushing...")
        project.git.push().setPushTags().call()
    }
}
