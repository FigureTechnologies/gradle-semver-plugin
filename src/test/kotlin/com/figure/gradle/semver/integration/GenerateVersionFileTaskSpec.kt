/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.integration

import com.figure.gradle.semver.internal.git.GitRef
import com.figure.gradle.semver.testkit.GradleIntegrationTestKitExtension
import com.figure.gradle.semver.util.GradleArgs
import com.figure.gradle.semver.util.runTask
import com.figure.gradle.semver.util.taskOutcome
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.exist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class GenerateVersionFileTaskSpec : FunSpec({
    val runner = GradleRunner.create()

    val gradleIntegrationTestKitExtension = GradleIntegrationTestKitExtension(runner, GitRef.Branch.MAIN)
    listener(gradleIntegrationTestKitExtension)

    val task = "generateVersionFile"
    val defaultArguments = listOf("build", task, GradleArgs.STACKTRACE)

    fun validateVersionFile() {
        val versionFile = gradleIntegrationTestKitExtension.tempRepoDir.resolve("build/semver/version.txt")

        versionFile should exist()
        versionFile.readText() shouldContain "9.9.9"
    }

    test("build") {
        // When
        val (firstRun, secondRun) = runner.runTask(defaultArguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.UP_TO_DATE

        validateVersionFile()
    }

    test("with parallel") {
        // Given
        val arguments = defaultArguments + listOf(GradleArgs.PARALLEL)

        // When
        val (firstRun, secondRun) = runner.runTask(arguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.UP_TO_DATE

        validateVersionFile()
    }

    test("with build-cache") {
        // Given
        val arguments = defaultArguments + listOf(GradleArgs.BUILD_CACHE)

        // When
        val (firstRun, secondRun) = runner.runTask(arguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.UP_TO_DATE

        validateVersionFile()
    }

    test("with configuration-cache") {
        // Given
        val arguments = defaultArguments + listOf(GradleArgs.CONFIGURATION_CACHE)

        // When
        val (firstRun, secondRun) = runner.runTask(arguments)

        // Then
        firstRun.output shouldContain "Calculating task graph as no cached configuration is available for tasks"

        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.UP_TO_DATE

        validateVersionFile()
    }

    test("with parallel, build-cache, and configuration-cache") {
        // Given
        val arguments =
            defaultArguments +
                listOf(
                    GradleArgs.PARALLEL,
                    GradleArgs.BUILD_CACHE,
                    GradleArgs.CONFIGURATION_CACHE,
                )

        // When
        val (firstRun, secondRun) = runner.runTask(arguments)

        // Then
        firstRun.output shouldContain "Calculating task graph as no cached configuration is available for tasks"

        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.UP_TO_DATE

        validateVersionFile()
    }
})
