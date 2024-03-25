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
import com.figure.gradle.semver.util.taskOutcome
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class CreateAndPushVersionTagTaskSpec : FunSpec({
    val runner = GradleRunner.create()

    val task = "createAndPushVersionTag"
    val defaultArguments = listOf("build", task, GradleArgs.STACKTRACE)

    fun GradleRunner.runWithoutExpectations(arguments: List<String>): Pair<BuildResult, BuildResult> {
        val firstRun =
            this
                .withArguments(arguments)
                .build()

        val secondRun =
            this
                .withArguments(arguments)
                .run()

        return firstRun to secondRun
    }

    context("create and push version tag") {
        val listeners =
            listOf(
                GradleIntegrationTestKitExtension(
                    runner,
                    initialBranch = GitRef.Branch.MAIN,
                ),
                GradleIntegrationTestKitExtension(
                    runner,
                    initialBranch = GitRef.Branch.MASTER,
                ),
                GradleIntegrationTestKitExtension(
                    runner,
                    initialBranch = GitRef.Branch.MAIN,
                    defaultBranch = GitRef.Branch.DEVELOP,
                ),
                GradleIntegrationTestKitExtension(
                    runner,
                    initialBranch = GitRef.Branch.MASTER,
                    defaultBranch = GitRef.Branch.DEVELOP,
                ),
            )

        val testData =
            listOf(
                TestData(listOf()),
                TestData(listOf(GradleArgs.PARALLEL)),
                TestData(listOf(GradleArgs.BUILD_CACHE)),
                TestData(listOf(GradleArgs.CONFIGURATION_CACHE)),
                TestData(listOf(GradleArgs.PARALLEL, GradleArgs.BUILD_CACHE, GradleArgs.CONFIGURATION_CACHE)),
            )

        listeners.forEach { listener ->
            listener(listener)

            withData(
                nameFn = {
                    if (listener.defaultBranch != null) {
                        "${listener.initialBranch.name}-${listener.defaultBranch.name} -- args: ${it.additionalArgs}"
                    } else {
                        "${listener.initialBranch.name} -- args: ${it.additionalArgs}"
                    }
                },
                testData.asSequence(),
            ) {
                // Given
                val arguments = defaultArguments + it.additionalArgs

                // When
                val (firstRun, secondRun) = runner.runWithoutExpectations(arguments)

                // Then
                firstRun.taskOutcome(task) shouldBe TaskOutcome.SUCCESS
                secondRun.taskOutcome(task) shouldBe TaskOutcome.FAILED
                secondRun.output shouldContain "TagAlreadyExistsException"
            }
        }
    }
})

private data class TestData(
    val additionalArgs: List<String>,
)
