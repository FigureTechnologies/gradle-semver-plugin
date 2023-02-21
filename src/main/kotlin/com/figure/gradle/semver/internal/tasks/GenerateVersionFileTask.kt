/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

//@DisableCachingByDefault(because = "Not worth caching")
internal abstract class GenerateVersionFileTask : DefaultTask() {

    @get:OutputFile
    abstract val destination: RegularFileProperty

    @get:Input
    internal abstract val version: Property<String>

    @get:Input
    internal abstract val versionTagName: Property<String>

    @TaskAction
    internal fun generateVersionFile() {
        val file = destination.get().asFile

        file.apply {
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
