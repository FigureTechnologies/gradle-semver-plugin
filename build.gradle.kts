/*
 * Copyright (C) 2024 Figure Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.adarshr.gradle.testlogger.theme.ThemeType
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publish.plugin)

    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
    alias(libs.plugins.best.practices)
    alias(libs.plugins.github.release)
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.binary.compatibility.validator)

    alias(libs.plugins.test.logger)
    alias(libs.plugins.gradle.testkit)

    idea
    if (System.getenv("CI") == "true") {
        signing
    }
}

group = "com.figure.gradle"
version = "2.0.0"
description = "Gradle Plugin for Automated Semantic Versioning"

val testImplementation: Configuration by configurations.getting

val functionalTestImplementation: Configuration by configurations.getting {
    extendsFrom(testImplementation)
}

sourceSets {
    named("functionalTest") {
        compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
        runtimeClasspath += output + compileClasspath
    }
}

dependencies {
    implementation(gradleKotlinDsl())

    implementation(libs.jgit)
    implementation(libs.kotlin.semver)

    testImplementation(gradleTestKit())
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.datatest)

    functionalTestImplementation(libs.testkit.support)
}

tasks {
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        // To be able to use withEnvironment: https://github.com/kotest/kotest/issues/2849
        jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED")
        testLogging {
            showStandardStreams = false
            showCauses = true
            showStackTraces = true
            events = setOf(SKIPPED, FAILED, STANDARD_ERROR)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }

    check {
        dependsOn("detekt")
    }

    withType<Detekt>().configureEach {
        reports {
            txt.required.set(true)
            html.required.set(true)
            xml.required.set(false)
            sarif.required.set(false)
        }
    }

    register("fmt") {
        group = "verification"
        description = "Format all code using configured formatters. Runs 'spotlessApply'"
        dependsOn("spotlessApply")
    }

    register("lint") {
        group = "verification"
        description = "Check all code using configured linters. Runs 'spotlessCheck'"
        dependsOn("spotlessCheck")
    }
}

idea {
    module {
        // Marks the functionTest as a test source set
        testSources.from(sourceSets.functionalTest.get().allSource.srcDirs)
    }
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.from(files("detekt.yml"))
}

spotless {
    format("misc") {
        target("*.md", "*.gitignore", "*.yml", "*.yaml", "*.toml", "*.properties")
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlin {
        target("src/**/*.kt")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeaderFile(rootProject.file("spotless/license.kt"))
    }

    kotlinGradle {
        target("*.kts", "src/**/*.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeaderFile(
            rootProject.file("spotless/license.kt"),
            "(import|plugins|buildscript|dependencies|pluginManagement|dependencyResolutionManagement)",
        )
    }
}

testlogger {
    theme = ThemeType.STANDARD
    showCauses = true
    slowThreshold = 1000
    showSummary = true
    showStandardStreams = false
}

apiValidation {
    ignoredPackages += listOf(
        // Internal package is not part of the public API
        "com.figure.gradle.semver.internal",
    )
}

class PublishingConstants {
    val name = "Gradle Semver Plugin"
    val description = "Gradle Plugin for Automatic Semantic Versioning"
    val pluginImplementation = "$group.semver.SemverPlugin"
    val tags = listOf("semver", "versioning", "git")

    val website = "https://figuretechnologies.github.io/gradle-semver-plugin"
    val vcsUrl = "https://github.com/FigureTechnologies/gradle-semver-plugin.git"
    val scmUrl = "scm:git:git://github.com/FigureTechnologies/gradle-semver-plugin.git"
}

val info = PublishingConstants()

gradlePlugin {
    website = info.website
    vcsUrl = info.vcsUrl
    plugins {
        register("semver") {
            id = "$group.semver-plugin"
            displayName = info.name
            description = info.description
            implementationClass = info.pluginImplementation
        }
    }
}

afterEvaluate {
    publishing {
        publications.filterIsInstance<MavenPublication>().forEach {
            it.pom {
                name = info.name
                description = info.description
                licenses {
                    license {
                        name = "The Apache Software License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "figure-oss"
                        name = "Figure OSS Engineers"
                        email = "oss@figure.com"
                    }
                    developer {
                        id = "tcrawford-figure"
                        name = "Tyler Crawford"
                        email = "tcrawford@figure.com"
                    }
                    developer {
                        id = "ahatzz11"
                        name = "Alex Hatzenbuhler"
                        email = "ahatzenbuhler@figure.com"
                    }
                    developer {
                        id = "jonasg13"
                        name = "Jonas Gorauskas"
                        email = "jgorauskas@figure.com"
                    }
                }
                scm {
                    connection = info.scmUrl
                    developerConnection = info.scmUrl
                    url = info.website
                }
            }
        }
    }
}

githubRelease {
    generateReleaseNotes = true
    owner = "FigureTechnologies"
    providers.environmentVariable("GITHUB_TOKEN").map { token(it) }
}
