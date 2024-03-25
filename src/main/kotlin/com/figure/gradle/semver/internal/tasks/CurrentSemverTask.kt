/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.tasks

import com.figure.gradle.semver.internal.semverLifecycle
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class CurrentSemverTask : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val versionTagName: Property<String>

    @TaskAction
    fun currentSemver() {
        logger.semverLifecycle("version: ${version.get()}")
        logger.semverLifecycle("versionTagName: ${versionTagName.get()}")
    }
}
