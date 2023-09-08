/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.tasks

import com.figure.gradle.semver.internal.exceptions.TagAlreadyExistsException
import com.figure.gradle.semver.internal.semverLifecycle
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class CreateAndPushVersionTagTask : DefaultTask() {
    @get:Input
    abstract val versionTagName: Property<String>

    @TaskAction
    fun createAndPushTag() {
        val git = Git(
            FileRepositoryBuilder()
                .readEnvironment()
                .findGitDir()
                .build()
        )

        val versionTag = versionTagName.get()

        val tags = git.tagList().call()
        val tagAlreadyExists = versionTag in tags.map { it.name.replace("refs/tags/", "") }

        if (tagAlreadyExists) throw TagAlreadyExistsException(versionTag)

        git.tag().setName(versionTag).call()
        git.push().setPushTags().call()
        logger.semverLifecycle("Created and pushed version tag: $versionTag")
    }
}
