/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.functional

import com.figure.gradle.semver.testkit.GradleFunctionalTestKitExtension
import com.figure.gradle.semver.util.GradleArgs
import com.figure.gradle.semver.util.taskOutcome
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class CreateAndPushVersionTagTaskFunctionalSpec : FunSpec({
    val runner = GradleRunner.create()

    val gradleFunctionalTestKitExtension = GradleFunctionalTestKitExtension(runner)
    listener(gradleFunctionalTestKitExtension)

    val task = "createAndPushVersionTag"
    val defaultArguments = listOf(task, GradleArgs.Stacktrace)

    fun GradleRunner.runWithoutExpectations(arguments: List<String>): Pair<BuildResult, BuildResult> {
        val firstRun = this
            .withArguments(arguments)
            .build()

        val secondRun = this
            .withArguments(arguments)
            .run()

        return firstRun to secondRun
    }

    test("no arguments") {
        // When
        val (firstRun, secondRun) = runner.runWithoutExpectations(defaultArguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.FAILED
        secondRun.output shouldContain "TagAlreadyExistsException"
    }

    test("with parallel") {
        // Given
        val arguments = defaultArguments + listOf(GradleArgs.Parallel)

        // When
        val (firstRun, secondRun) = runner.runWithoutExpectations(arguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.FAILED
        secondRun.output shouldContain "TagAlreadyExistsException"
    }

    test("with build-cache") {
        // Given
        val arguments = defaultArguments + listOf(GradleArgs.BuildCache)

        // When
        val (firstRun, secondRun) = runner.runWithoutExpectations(arguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.FAILED
        secondRun.output shouldContain "TagAlreadyExistsException"
    }

    test("with configuration-cache") {
        // Given
        val arguments = defaultArguments + listOf(GradleArgs.ConfigurationCache)

        // When
        val (firstRun, secondRun) = runner.runWithoutExpectations(arguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.FAILED
        secondRun.output shouldContain "TagAlreadyExistsException"
    }

    test("with parallel, build-cache, and configuration-cache") {
        // Given
        val arguments = defaultArguments + listOf(
            GradleArgs.Parallel,
            GradleArgs.BuildCache,
            GradleArgs.ConfigurationCache,
        )

        // When
        val (firstRun, secondRun) = runner.runWithoutExpectations(arguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.FAILED
        secondRun.output shouldContain "TagAlreadyExistsException"
    }
})
