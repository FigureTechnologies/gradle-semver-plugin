/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.tasks

import com.figure.gradle.semver.internal.semverLifecycle
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

internal abstract class PrintVersionTask : DefaultTask() {
    @get:Input
    internal abstract val version: Property<String>

    @TaskAction
    internal fun printVersion() {
        logger.semverLifecycle("Semantic version: ${version.get()}")
    }
}
