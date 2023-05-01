/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.functional

import com.figure.gradle.semver.testkit.GradleFunctionalTestKitExtension
import com.figure.gradle.semver.util.GradleArgs
import com.figure.gradle.semver.util.NEXT_PATCH_VERSION
import com.figure.gradle.semver.util.resourceFromPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner

class ComputeNextVersionFunctionalSpec : FunSpec({

    val runner = GradleRunner.create()

    val gradleFunctionalTestKitExtension = GradleFunctionalTestKitExtension(
        runner = runner,
        buildFile = resourceFromPath("functional/standard-project/build.gradle.kts"),
        settingsFile = resourceFromPath("functional/standard-project/settings.gradle.kts")
    )

    listener(gradleFunctionalTestKitExtension)

    test("should compute next version") {
        // Given
        val git = gradleFunctionalTestKitExtension.git

        git.commit().setMessage("Empty commit").setAllowEmpty(true).call()
        git.push().call()

        println("Using project dir: ${runner.projectDir}")

        // When
        val buildResult = runner
            .withArguments(GradleArgs.Stacktrace)
            .build()

        // Then
        buildResult.output shouldContain NEXT_PATCH_VERSION
    }

    test("should compute next version with additional params") {
        // Given
        val git = gradleFunctionalTestKitExtension.git

        git.commit().setMessage("Empty commit").setAllowEmpty(true).call()
        git.push().call()

        println("Using project dir: ${runner.projectDir}")

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
