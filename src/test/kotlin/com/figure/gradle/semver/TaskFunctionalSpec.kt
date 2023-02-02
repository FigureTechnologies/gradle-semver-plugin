/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class TaskFunctionalSpec : FunSpec({
    lateinit var directory: File
    lateinit var buildFile: File
    lateinit var settingsFile: File

    lateinit var runner: GradleRunner

    // withData() is a TestType, not a TestCase, which needs beforeAny{}
    beforeAny {
        directory = tempdir()
        withContext(Dispatchers.IO) {
            directory.createNewFile()
        }

        // KILL GIT
        File("$directory/.git").deleteRecursively()

        buildFile = File("$directory", "build.gradle.kts")
        settingsFile = File("$directory", "settings.gradle.kts")

        buildFile.writeText(
            """
        plugins {
            id("com.figure.gradle.semver-plugin")
        }

        semver {
            overrideVersion("9.9.9")
        }
            """.trimIndent()
        )

        settingsFile.writeText(
            """
        rootProject.name = "task-tester"
            """.trimIndent()
        )

        runner = GradleRunner.create()
            .withProjectDir(File(directory.path.toString()))
            .withPluginClasspath()
    }

    test("currentSemver") {
        val run = runner
            .withArguments("currentSemver")
            .build()

        print(run.output)

        run.output.shouldContain("version: ")
        run.output.shouldContain("versionTagName: ")
    }
})
