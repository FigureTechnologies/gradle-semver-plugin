/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v1

import com.figure.gradle.semver.v1.internal.git.git
import com.figure.gradle.semver.v1.tasks.CreateAndPushVersionTag
import com.figure.gradle.semver.v1.tasks.GenerateVersionFileTask
import com.figure.gradle.semver.v1.tasks.PrintVersionTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.nio.file.Files

class SemverPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val semver = project.extensions.create<SemverExtension>("semver")

        project.tasks.register<PrintVersionTask>("printVersion") {
            version.set(semver.version)
        }

        project.tasks.register<GenerateVersionFileTask>("generateVersionFile") {
            // Ensure the build directory exists first
            if (!project.buildDir.exists()) {
                Files.createDirectory(project.buildDir.toPath())
            }

            buildDir.set(project.buildDir)
            version.set(semver.version)
            versionTagName.set(semver.versionTagName)
        }

        project.tasks.register<CreateAndPushVersionTag>("createAndPushVersionTag") {
            versionTagName.set(semver.versionTagName)
            git.set(project.git(semver.gitDir.get()))
        }
    }
}
