/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v1.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateVersionFileTask : DefaultTask() {
    @get:InputDirectory
    abstract val buildDir: Property<File>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val versionTagName: Property<String>

    @TaskAction
    fun generateVersionFile() {
        val filePath = "${buildDir.get()}/semver/version.txt"
        logger.quiet("Generating version file at $filePath")
        File(filePath).apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(
                """
                    |${version.get()}
                    |${versionTagName.get()}
                """.trimMargin()
            )
        }
    }
}
