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
import org.gradle.api.tasks.TaskAction

abstract class PrintVersionTask : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    @TaskAction
    fun printVersion() {
        logger.quiet("Semantic version: ${version.get()}")
    }
}
