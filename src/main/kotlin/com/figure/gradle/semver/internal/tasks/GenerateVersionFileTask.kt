/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.tasks

import com.figure.gradle.semver.internal.semverDebug
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Not worth caching")
internal abstract class GenerateVersionFileTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    internal abstract val buildDir: Property<File>

    @get:Input
    internal abstract val version: Property<String>

    @get:Input
    internal abstract val versionTagName: Property<String>

    @TaskAction
    internal fun generateVersionFile() {
        val filePath = "${buildDir.get()}/semver/version.txt"
        logger.semverDebug("Generating version file at $filePath")
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
