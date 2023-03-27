/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.tasks

import com.figure.gradle.semver.internal.semverLifecycle
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * UntrackedTask - TL;DR - will make sure that the task always runs and is always out of date
 */
@UntrackedTask(
    because = "Git already takes care of keeping the state. There are issues with using Gradle caching and this task."
)
@DisableCachingByDefault
abstract class CreateAndPushVersionTagTaskV2 : DefaultTask() {
    @get:Input
    abstract val versionTagName: Property<String>

    @get:InputDirectory
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

        git.tag().setName(versionTagName.get()).call()
        git.push().setPushTags().call()
        logger.semverLifecycle("Created and pushed version tag: ${versionTagName.get()}")
    }
}
