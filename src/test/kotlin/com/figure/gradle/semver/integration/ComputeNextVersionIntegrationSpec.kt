/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.integration

import com.figure.gradle.semver.testkit.GradleIntegrationTestKitExtension
import com.figure.gradle.semver.util.GradleArgs
import com.figure.gradle.semver.util.resourceFromPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.GradleRunner

class ComputeNextVersionIntegrationSpec : FunSpec({

    val runner = GradleRunner.create()

    val gradleIntegrationTestKitExtension = GradleIntegrationTestKitExtension(
        runner = runner,
        buildFile = resourceFromPath("integration/standard-project/build.gradle.kts"),
        settingsFile = resourceFromPath("integration/standard-project/settings.gradle.kts")
    )

    listener(gradleIntegrationTestKitExtension)

    test("should compute next version") {
        // Given
        val git = Git.open(gradleIntegrationTestKitExtension.tempDirectory)

        git.commit().setMessage("Empty commit").setAllowEmpty(true).call()
        git.push().call()

        // When
        val buildResult = runner
            .withArguments(GradleArgs.Stacktrace)
            .build()

        // Then
        buildResult.output shouldContain "1.0.3"
    }

    test("should compute next version with additional params") {
        // Given
        val git = Git.open(gradleIntegrationTestKitExtension.tempDirectory)

        // git.pull().call()
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
        buildResult.output shouldContain "1.0.3"
    }
})
