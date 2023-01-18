/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v1.tasks

import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class CreateAndPushVersionTag : DefaultTask() {
    @get:Input
    abstract val versionTagName: Property<String>

    @get:Input
    abstract val git: Property<Git>

    @TaskAction
    fun createAndPushTag() {
        git.get().tag().setName(versionTagName.get()).call()
        git.get().push().setPushTags().call()
        logger.quiet("Created and pushed version tag: ${versionTagName.get()}")
    }
}
