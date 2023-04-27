/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.functional

import com.figure.gradle.semver.testkit.GradleFunctionalTestKitExtension
import com.figure.gradle.semver.util.GradleArgs
import com.figure.gradle.semver.util.runTask
import com.figure.gradle.semver.util.taskOutcome
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class CurrentSemverTaskFunctionalSpec : FunSpec({
    val runner = GradleRunner.create()

    val gradleFunctionalTestKitExtension = GradleFunctionalTestKitExtension(runner)
    listener(gradleFunctionalTestKitExtension)

    val task = "currentSemver"
    val defaultArguments = listOf(task, GradleArgs.Stacktrace)

    test("no arguments") {
        // When
        val (firstRun, secondRun) = runner.runTask(defaultArguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS

        firstRun.output shouldContain "version: 9.9.9"
        secondRun.output shouldContain "version: 9.9.9"
    }

    test("with parallel") {
        // Given
        val arguments = defaultArguments + listOf(GradleArgs.Parallel)

        // When
        val (firstRun, secondRun) = runner.runTask(arguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS

        firstRun.output shouldContain "version: 9.9.9"
        secondRun.output shouldContain "version: 9.9.9"
    }

    test("with build-cache") {
        // Given
        val arguments = defaultArguments + listOf(GradleArgs.BuildCache)

        // When
        val (firstRun, secondRun) = runner.runTask(arguments)

        // Then
        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS

        firstRun.output shouldContain "version: 9.9.9"
        secondRun.output shouldContain "version: 9.9.9"
    }

    test("with configuration-cache") {
        // Given
        val arguments = defaultArguments + listOf(GradleArgs.ConfigurationCache)

        // When
        val (firstRun, secondRun) = runner.runTask(arguments)

        // Then
        firstRun.output shouldContain "no configuration cache is available for tasks: $task"
        secondRun.output shouldContain "Reusing configuration cache."

        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS

        firstRun.output shouldContain "version: 9.9.9"
        secondRun.output shouldContain "version: 9.9.9"
    }

    test("with parallel, build-cache, and configuration-cache") {
        // Given
        val arguments = defaultArguments + listOf(
            GradleArgs.Parallel,
            GradleArgs.BuildCache,
            GradleArgs.ConfigurationCache,
        )

        // When
        val (firstRun, secondRun) = runner.runTask(arguments)

        // Then
        firstRun.output shouldContain "no configuration cache is available for tasks: $task"
        secondRun.output shouldContain "Reusing configuration cache."

        firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
        secondRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS

        firstRun.output shouldContain "version: 9.9.9"
        secondRun.output shouldContain "version: 9.9.9"
    }
})
