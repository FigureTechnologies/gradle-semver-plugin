/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver

import com.figure.gradle.semver.internal.tasks.CreateAndPushVersionTagTask
import com.figure.gradle.semver.internal.tasks.CurrentSemverTask
import com.figure.gradle.semver.internal.tasks.GenerateVersionFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File
import java.nio.file.Files

class SemverPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val semver = project.extensions.create<SemverExtension>("semver")

        project.tasks.register<CurrentSemverTask>("currentSemver") {
            version.set(semver.version)
            versionTagName.set(semver.versionTagName)
        }

        project.tasks.register<GenerateVersionFileTask>("generateVersionFile") {
            // Ensure the build directory exists first
            if (!project.buildDir.exists()) {
                Files.createDirectory(project.buildDir.toPath())
            }

            val versionFile = File("${project.buildDir}/semver/version.txt")

            destination.fileValue(versionFile)
            version.set(semver.version)
            versionTagName.set(semver.versionTagName)
        }

        project.tasks.register<CreateAndPushVersionTagTask>("createAndPushVersionTag") {
            this.versionTagName.set(semver.versionTagName)
            this.gitDir.set(project.file(semver.gitDir.get()))
        }

        // project.tasks.create()
        //
        // project.tasks.named<CreateAndPushVersionTagTask>("createAndPushVersionTag") {
        // }

        // project.tasks.register<DefaultTask>("createAndPushVersionTag") {
        //     notCompatibleWithConfigurationCache("No reason to cache")
        //     doNotTrackState("No reason to cache")
        //     outputs.upToDateWhen { false }
        //     doLast {
        //         val git = project.git(semver.gitDir.get())
        //         val versionTagName = semver.versionTagName
        //
        //         git.tag().setName(versionTagName).call()
        //         git.push().setPushTags().call()
        //         logger.semverLifecycle("Created and pushed version tag: $versionTagName")
        //     }
        // }
    }
}
