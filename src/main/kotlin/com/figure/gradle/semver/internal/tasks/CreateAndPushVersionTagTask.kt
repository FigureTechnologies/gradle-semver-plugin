package com.figure.gradle.semver.internal.tasks

import com.figure.gradle.semver.internal.semverLifecycle
import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * UntrackedTask - TL;DR - will make sure that the task always runs and is always out of date
 */
@UntrackedTask(
    because = "Git already takes care of keeping the state. There are issues with using Gradle caching and this task."
)
abstract class CreateAndPushVersionTagTask : DefaultTask() {
    @get:Input
    abstract val versionTagName: Property<String>

    @get:Input
    abstract val git: Property<Git>

    @TaskAction
    fun createAndPushTag() {
        git.get().tag().setName(versionTagName.get()).call()
        git.get().push().setPushTags().call()
        logger.semverLifecycle("Created and pushed version tag: ${versionTagName.get()}")
    }
}
