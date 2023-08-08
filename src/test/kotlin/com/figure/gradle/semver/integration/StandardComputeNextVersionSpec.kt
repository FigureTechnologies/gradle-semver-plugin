/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.integration

import com.figure.gradle.semver.internal.git.GitRef
import com.figure.gradle.semver.testkit.GradleIntegrationTestKitExtension
import com.figure.gradle.semver.util.GradleArgs
import com.figure.gradle.semver.util.NEXT_PATCH_VERSION
import com.figure.gradle.semver.util.resourceFromPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner

/**
 * These tests are disabled since there is an issue with calculating the current branch when running in GitHub Actions.
 *
 * When the plugin executes to calculate the current branch, we have to take into account whether we're in a
 * GitHub Action since the format for the "current branch" when executing from a pull request is
 * "refs/pull/<pr_number>/merge". Obviously, we don't want to use this as the "current branch" when we're building an
 * actual project.
 *
 * The problem comes in when we run a test. We have these environment variables set for GITHUB_* and so we utilize them
 * during a test run. This causes the incorrect branch to be fetched. That is, the PR branch gets used instead of
 * the branch current branch in the GradleRunner.
 *
 * TL;DR - Tests should work locally, tests will not work in GitHub Actions :(
 */
class StandardComputeNextVersionSpec : FunSpec({

    val runner = GradleRunner.create()

    val gradleIntegrationTestKitExtension = GradleIntegrationTestKitExtension(
        runner = runner,
        initialBranch = GitRef.Branch.MAIN,
        defaultBranch = GitRef.Branch.DEVELOP,
        buildFile = resourceFromPath("integration/standard-project/build.gradle.kts"),
        settingsFile = resourceFromPath("integration/standard-project/settings.gradle.kts")
    )

    listener(gradleIntegrationTestKitExtension)

    xtest("should compute next version") {
        // Given
        val git = gradleIntegrationTestKitExtension.git

        git.commit().setMessage("Empty commit").setAllowEmpty(true).call()
        git.push().call()

        // When
        val buildResult = runner
            .withArguments(GradleArgs.Stacktrace)
            .build()

        // Then
        buildResult.output shouldContain NEXT_PATCH_VERSION
    }

    xtest("should compute next version with additional params") {
        // Given
        val git = gradleIntegrationTestKitExtension.git

        git.commit().setMessage("Empty commit").setAllowEmpty(true).call()
        git.push().call()

        // When
        val buildResult = runner
            .withArguments(
                GradleArgs.Stacktrace,
                GradleArgs.Parallel,
                GradleArgs.BuildCache,
                GradleArgs.ConfigurationCache
            )
            .build()

        // Then
        buildResult.output shouldContain NEXT_PATCH_VERSION
    }
})
