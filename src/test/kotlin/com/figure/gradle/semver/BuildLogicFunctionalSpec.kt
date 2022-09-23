package com.figure.gradle.semver

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class BuildLogicFunctionalSpec : FunSpec({

//    val directory = File(tempdir().path.removePrefix("/private"))
    val directory = tempdir()
    lateinit var buildFile: File
    lateinit var settingsFile: File

//    val runner =

    beforeEach {
        buildFile = File("$directory", "build.gradle.kts")
        settingsFile = File("$directory", "settings.gradle.kts")

    }

    test("configuration-cache") {

        buildFile.writeText("""
        plugins {
            id("com.figure.gradle.semver-plugin")
        }
        """)

        settingsFile.writeText("""
        rootProject.name = "config-cache-tester"
        """.trimIndent())

//        withContext(Dispatchers.IO) {
//            Thread.sleep(10000)
//        }

//        Runtime.getRuntime().exec("cd $directory; git init")

        // first one loads the cache
        GradleRunner.create()
            .withProjectDir(File("/private/" + directory.path.toString()))
            .withPluginClasspath()
            .withArguments("--configuration-cache", "generateVersionFile")
            .build()
//
//        // second one uses the cache
//        val result = runner
//            .withArguments("--configuration-cache", "generateVersionFile")
//            .build()
//
//        result.output shouldContain "Reusing configuration cache."
        // ... more assertions on your task behavior
    }
})
