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

class BuildLogicFunctionalSpec : FunSpec({
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
        rootProject.name = "config-cache-tester"
            """.trimIndent()
        )

        runner = GradleRunner.create()
            .withProjectDir(File(directory.path.toString()))
            .withPluginClasspath()
    }

    context("configuration-cache") {
        withData(
            "printVersion",
            "generateVersionFile",
            // sadly, by using the Git class in this task we can't support the configuration-cache, see bottom of class
//            "createAndPushVersionTag"
        ) { task: String ->
            // first one loads the cache
            val firstRun = runner
                .withArguments(task, "--configuration-cache", "--scan")
                .build()

            // second one uses the cache
            val secondRun = runner
                .withArguments(task, "--configuration-cache", "--scan")
                .build()

            firstRun.output shouldContain "0 problems were found storing the configuration cache."
            firstRun.output shouldContain "Configuration cache entry stored."

            secondRun.output shouldContain "Reusing configuration cache."
        }
    }
})

/**
 * Regarding configuration-cache support for the `createAndPushVersionTag` task
 * This becomes a big hole that results in Instant.writeReplace() not being accessible.
 * > Unable to make private java.lang.Object java.time.Instant.writeReplace() accessible:
 *     module java.base does not "opens java.time" to unnamed module
 *
 * https://stackoverflow.com/a/70878195/2785519
 * I tried to fix this with --add-opens java.base/java.time=ALL-UNNAMED in the test and on the base level build as a JVM
 * argument, but that didn't work. For now we are just going to make a note that this task is not supported.
 * We can continue looking into it for the future, but at least we have a test that will validate for us.
 */
