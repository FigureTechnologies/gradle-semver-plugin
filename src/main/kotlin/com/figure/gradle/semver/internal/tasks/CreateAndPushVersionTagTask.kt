/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.tasks

// import com.figure.gradle.semver.internal.semverLifecycle
// import org.eclipse.jgit.api.Git
// import org.gradle.api.DefaultTask
// import org.gradle.api.tasks.TaskAction
// import org.gradle.api.tasks.UntrackedTask
// import javax.inject.Inject

/**
 * UntrackedTask - TL;DR - will make sure that the task always runs and is always out of date
 */
// @UntrackedTask(
//     because = "Git already takes care of keeping the state. There are issues with using Gradle caching and this task."
// )
// abstract class CreateAndPushVersionTagTask @Inject constructor(
//     private val versionTagName: String,
//     private val git: Git,
// ) : DefaultTask() {
//     @TaskAction
//     fun createAndPushTag() {
//         git.tag().setName(versionTagName).call()
//         git.push().setPushTags().call()
//         logger.semverLifecycle("Created and pushed version tag: $versionTagName")
//     }
// }
