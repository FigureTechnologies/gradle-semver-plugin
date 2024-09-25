/*
 * Copyright (C) 2024 Figure Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.figure.gradle.semver.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import java.io.File

fun build(
    gradleVersion: GradleVersion,
    projectDir: File,
    vararg args: String,
): BuildResult =
    runner(gradleVersion, projectDir, *args).build()

fun buildAndFail(
    gradleVersion: GradleVersion,
    projectDir: File,
    vararg args: String,
): BuildResult =
    runner(gradleVersion, projectDir, *args).buildAndFail()

fun runWithoutExpectations(
    gradleVersion: GradleVersion,
    projectDir: File,
    vararg args: String,
): BuildResult =
    runner(gradleVersion, projectDir, *args).run()

fun runner(
    gradleVersion: GradleVersion,
    projectDir: File,
    vararg args: String,
): GradleRunner = GradleRunner.create().apply {
    val arguments = setOf(
        *args,
        "--parallel",
        "--build-cache",
        "--configuration-cache",
        "--stacktrace",
        "-Psemver.forTesting=true",
    ).toList()

    forwardOutput()
    withGradleVersion(gradleVersion.version)
    withProjectDir(projectDir)
    withArguments(arguments)
}
