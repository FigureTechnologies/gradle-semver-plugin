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
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import java.io.File

public class SemverPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.semver() // impure! but needed to create and register the extension with the project so that we can use it in the tasks below
        if (!target.hasGit)
            target.logger.warn("the current directory is not part of a git repo, cannot determine project semantic version number, please initialize a git repo with main & develop branches")

        target.tasks.register("cv", CurrentVersionTask::class.java)
        target.tasks.register("generateVersionFile", GenerateVersionFileTask::class.java)
        target.tasks.register("createAndPushVersionTag", CreateAndPushVersionTag::class.java)
    }
}

open class CurrentVersionTask: DefaultTask() {
    @TaskAction
    fun currentVersion() {
        project.logger.lifecycle("version: ${(project.extensions[SemverExtension.ExtensionName] as SemverExtension).version}".purple())
    }
}

open class GenerateVersionFileTask: DefaultTask() {
    @TaskAction
    fun generateVersionFile() {
        val extension = (project.extensions[SemverExtension.ExtensionName] as SemverExtension)
        with (project) {
            File("$buildDir/semver/version.txt").apply {
                parentFile.mkdirs()
                createNewFile()
                writeText(
                    """
                       |${extension.version}
                       |${extension.versionTagName}
                    """.trimMargin()
                )
            }
        }
    }
}

open class CreateAndPushVersionTag: DefaultTask() {
    @TaskAction
    fun createAndPushTag() {
        val extension = (project.extensions[SemverExtension.ExtensionName] as SemverExtension)
        project.git.tag().setName(extension.versionTagName).call()
        project.logger.semver("created version tag: ${extension.versionTagName}, pushing...")
        project.git.push().setPushTags().call()
    }
}
