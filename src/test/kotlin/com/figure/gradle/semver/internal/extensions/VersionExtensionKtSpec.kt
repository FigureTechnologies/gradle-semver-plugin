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
package com.figure.gradle.semver.internal.extensions

import com.figure.gradle.semver.internal.properties.Modifier
import com.figure.gradle.semver.internal.properties.Stage
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersion
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class VersionExtensionKtSpec : FunSpec({
    test("invalid version") {
        val inputVersion = "1.0.0-my-cool-branch".toVersion()
        shouldThrowAny {
            inputVersion.nextVersion(Stage.Auto, Modifier.Auto)
        }
    }

    context("next version from stable") {
        withData(
            Stage.Dev,
            Stage.Alpha,
            Stage.Beta,
            Stage.ReleaseCandidate,
            Stage.Final,
            Stage.GA,
            Stage.Release,
        ) { stage ->
            val inputVersion = "1.0.0".toVersion()
            withData(
                TestData(Modifier.Major, "2.0.0-${stage.value}.1"),
                TestData(Modifier.Minor, "1.1.0-${stage.value}.1"),
                TestData(Modifier.Patch, "1.0.1-${stage.value}.1"),
                TestData(Modifier.Auto, "1.0.1-${stage.value}.1"),
            ) {
                inputVersion.nextVersion(stage, it.modifier) shouldBe it.expectedVersion
            }
        }

        withData(
            Stage.Stable,
            Stage.Auto,
        ) { stage ->
            val inputVersion = "1.0.0".toVersion()
            withData(
                TestData(Modifier.Major, "2.0.0"),
                TestData(Modifier.Minor, "1.1.0"),
                TestData(Modifier.Patch, "1.0.1"),
                TestData(Modifier.Auto, "1.0.1"),
            ) {
                inputVersion.nextVersion(stage, it.modifier) shouldBe it.expectedVersion
            }
        }

        context(Stage.Snapshot.name) {
            val inputVersion = "1.0.0".toVersion()
            val stage = Stage.Snapshot
            withData(
                TestData(Modifier.Major, "2.0.0-${stage.value}"),
                TestData(Modifier.Minor, "1.1.0-${stage.value}"),
                TestData(Modifier.Patch, "1.0.1-${stage.value}"),
                TestData(Modifier.Auto, "1.0.1-${stage.value}"),
            ) {
                inputVersion.nextVersion(stage, it.modifier) shouldBe it.expectedVersion
            }
        }
    }

    context("next version from pre-release") {
        withData(
            Stage.Dev,
            Stage.Alpha,
            Stage.Beta,
            Stage.ReleaseCandidate,
            Stage.Final,
            Stage.GA,
            Stage.Release,
        ) { stage ->
            val inputVersion = "1.0.0-${stage.value}.1".toVersion()

            withData(
                TestData(Modifier.Major, "2.0.0-${stage.value}.1"),
                TestData(Modifier.Minor, "1.1.0-${stage.value}.1"),
                TestData(Modifier.Patch, "1.0.1-${stage.value}.1"),
                TestData(Modifier.Auto, "1.0.0-${stage.value}.2"),
            ) {
                inputVersion.nextVersion(stage, it.modifier) shouldBe it.expectedVersion
            }
        }

        context(Stage.Stable.name) {
            val stage = Stage.Stable
            val inputVersion = "1.0.0-alpha.1".toVersion()

            withData(
                TestData(Modifier.Major, "2.0.0"),
                TestData(Modifier.Minor, "1.1.0"),
                TestData(Modifier.Patch, "1.0.0"),
                TestData(Modifier.Auto, "1.0.0"),
            ) {
                inputVersion.nextVersion(stage, it.modifier) shouldBe it.expectedVersion
            }
        }

        context(Stage.Auto.name) {
            val stage = Stage.Auto
            val inputVersion = "1.0.0-alpha.1".toVersion()

            withData(
                TestData(Modifier.Major, "2.0.0-alpha.1"),
                TestData(Modifier.Minor, "1.1.0-alpha.1"),
                TestData(Modifier.Patch, "1.0.1-alpha.1"),
                TestData(Modifier.Auto, "1.0.0-alpha.2"),
            ) {
                inputVersion.nextVersion(stage, it.modifier) shouldBe it.expectedVersion
            }
        }

        context("different pre-release") {
            withData(
                Stage.Dev,
                Stage.Beta,
                Stage.ReleaseCandidate,
                Stage.Final,
                Stage.GA,
                Stage.Release,
            ) { stage ->
                val inputVersion = "1.0.0-alpha.1".toVersion()
                withData(
                    TestData(Modifier.Major, "2.0.0-${stage.value}.1"),
                    TestData(Modifier.Minor, "1.1.0-${stage.value}.1"),
                    TestData(Modifier.Patch, "1.0.1-${stage.value}.1"),
                    TestData(Modifier.Auto, "1.0.1-${stage.value}.1"),
                ) {
                    inputVersion.nextVersion(stage, it.modifier) shouldBe it.expectedVersion
                }
            }
        }

        context(Stage.Snapshot.name) {
            val inputVersion = "1.0.0-alpha.1".toVersion()
            val stage = Stage.Snapshot
            withData(
                TestData(Modifier.Major, "2.0.0-${stage.value}"),
                TestData(Modifier.Minor, "1.1.0-${stage.value}"),
                TestData(Modifier.Patch, "1.0.1-${stage.value}"),
                TestData(Modifier.Auto, "1.0.1-${stage.value}"),
            ) {
                inputVersion.nextVersion(stage, it.modifier) shouldBe it.expectedVersion
            }
        }
    }
})

internal data class TestData(
    val modifier: Modifier,
    private val expectedVersionString: String,
) {
    val expectedVersion: Version
        get() = expectedVersionString.toVersion()
}
