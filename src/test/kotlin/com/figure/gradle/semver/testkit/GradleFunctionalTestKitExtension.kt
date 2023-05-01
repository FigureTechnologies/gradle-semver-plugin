/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.testkit

import com.figure.gradle.semver.util.appendFileContents
import com.figure.gradle.semver.util.copyToDir
import com.figure.gradle.semver.util.initializeWithCommitsAndTags
import com.figure.gradle.semver.util.resourceFromPath
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.io.path.createTempDirectory

class GradleFunctionalTestKitExtension(
    private val runner: GradleRunner,
    private val buildFile: File = resourceFromPath("functional/basic-project/build.gradle.kts"),
    private val settingsFile: File = resourceFromPath("functional/basic-project/settings.gradle.kts"),
) : TestListener {
    lateinit var tempRepoDir: File
    lateinit var tempRemoteRepoDir: File
    lateinit var localBuildCacheDirectory: File
    lateinit var git: Git

    override suspend fun beforeAny(testCase: TestCase) {
        tempRepoDir = createTempDirectory("tempRepoDir").toFile()
        tempRemoteRepoDir = createTempDirectory("tempRemoteRepoDir").toFile()

        buildFile.copyToDir(tempRepoDir, "build.gradle.kts")

        localBuildCacheDirectory = File(tempRepoDir, "local-cache")
        val updatedSettingsFile = settingsFile.appendFileContents(
            """
            buildCache {
                local {
                    directory = "${localBuildCacheDirectory.toURI()}"
                }
            }
            """.trimMargin()
        )

        updatedSettingsFile.copyToDir(tempRepoDir, "settings.gradle.kts")

        // Initialize temp directory as a "repo"
        git = Git.init().setDirectory(tempRepoDir).setInitialBranch("main").call()
        git.initializeWithCommitsAndTags(tempRepoDir, tempRemoteRepoDir)

        runner.forwardOutput()
            .withProjectDir(File(tempRepoDir.path.toString()))
            .withPluginClasspath()
    }

    override suspend fun afterAny(testCase: TestCase, result: TestResult) {
        tempRepoDir.deleteRecursively()
        tempRemoteRepoDir.deleteRecursively()
        localBuildCacheDirectory.deleteRecursively()
    }
}
