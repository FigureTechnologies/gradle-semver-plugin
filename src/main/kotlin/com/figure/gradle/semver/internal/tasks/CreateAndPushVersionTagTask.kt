/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
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
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class CreateAndPushVersionTagTask : DefaultTask() {
    @get:Input
    abstract val versionTagName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val gitDir: Property<File>

    @TaskAction
    fun createAndPushTag() {
        val git = Git(
            FileRepositoryBuilder()
                .setGitDir(gitDir.get())
                .readEnvironment()
                .findGitDir()
                .build()
        )

        val tags = git.tagList().call()
        val tagAlreadyExists = versionTagName.get() in tags.map { it.name.replace("refs/tags/", "") }

        if (tagAlreadyExists) throw TagAlreadyExistsException(versionTagName.get())

        git.tag().setName(versionTagName.get()).call()
        git.push().setPushTags().call()
        logger.semverLifecycle("Created and pushed version tag: ${versionTagName.get()}")
    }
}
