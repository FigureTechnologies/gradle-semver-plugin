/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v0

import com.figure.gradle.semver.v0.SemverExtension.Companion.semver
import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import java.io.File
import java.nio.file.Files

open class SemverPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.semver() // impure! but needed to create and register the extension with the project so that we can use it in the tasks below

        // Get the semver extension for properties we need - version and versionTagName
        val semverExtension = project.extensions[SemverExtension.ExtensionName] as SemverExtension

        val gitDir = semverExtension.getGitDir()

        if (!project.hasGit(gitDir)) {
            project.logger.warn("The directory $gitDir does not exist. If this should be the location of your git directory, please initialize a git repo")
        }

        project.tasks.register("cv", CurrentVersionTask::class.java) {
            it.version = semverExtension.version
        }

        project.tasks.register("generateVersionFile", GenerateVersionFileTask::class.java) {

            // Ensure that the buildDir actually exists to avoid some directory not found errors
            if (!project.buildDir.exists()) {
                Files.createDirectory(project.buildDir.toPath())
            }

            it.buildDir = project.buildDir
            it.version = semverExtension.version
            it.versionTagName = semverExtension.versionTagName
        }

        project.tasks.register("createAndPushVersionTag", CreateAndPushVersionTag::class.java) {
            it.versionTagName = semverExtension.versionTagName
            it.git = project.git(gitDir)
        }
    }
}

abstract class CurrentVersionTask : DefaultTask() {

    @get:Input
    abstract var version: String

    @TaskAction
    fun currentVersion() {
        logger.lifecycle("version: $version}".purple())
    }
}

abstract class GenerateVersionFileTask : DefaultTask() {

    @get:InputDirectory
    abstract var buildDir: File

    @get:Input
    abstract var version: String

    @get:Input
    abstract var versionTagName: String

    @TaskAction
    fun generateVersionFile() {
        File("${buildDir}/semver/version.txt").apply {
            this.parentFile.mkdirs()
            this.createNewFile()
            this.writeText(
                """
                   |${version}
                   |${versionTagName}
                """.trimMargin()
            )
        }
    }
}

abstract class CreateAndPushVersionTag : DefaultTask() {

    @get:Input
    abstract var versionTagName: String

    @get:Input
    abstract var git: Git

    @TaskAction
    fun createAndPushTag() {
        git.tag().setName(versionTagName).call()
        logger.semver("created version tag: ${versionTagName}, pushing...")
        git.push().setPushTags().call()
    }
}
