/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.util

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

fun GradleRunner.runTask(arguments: List<String>): Pair<BuildResult, BuildResult> {
    val firstRun =
        this
            .withArguments(arguments)
            .build()

    val secondRun =
        this
            .withArguments(arguments)
            .build()

    return firstRun to secondRun
}
