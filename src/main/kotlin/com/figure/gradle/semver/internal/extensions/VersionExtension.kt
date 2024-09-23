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

import com.figure.gradle.semver.internal.properties.BuildMetadataOptions
import com.figure.gradle.semver.internal.properties.BuildMetadataOptions.ALWAYS
import com.figure.gradle.semver.internal.properties.BuildMetadataOptions.LOCALLY
import com.figure.gradle.semver.internal.properties.BuildMetadataOptions.NEVER
import com.figure.gradle.semver.internal.properties.Modifier
import com.figure.gradle.semver.internal.properties.Stage
import io.github.z4kn4fein.semver.Inc
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.inc
import io.github.z4kn4fein.semver.nextPatch
import io.github.z4kn4fein.semver.nextPreRelease
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Version.nextVersion(providedStage: Stage, providedModifier: Modifier): Version = when {
    isInvalidVersionForComputation() -> {
        error(
            "Cannot compute next version because the latest computed version likely contains branch " +
                "information: $this. If you see this error, please file an issue. This is a bug.",
        )
    }

    // next snapshot
    (providedStage == Stage.Snapshot) -> {
        nextSnapshot(providedModifier.toInc())
    }

    // next pre-release
    (providedModifier == Modifier.Auto && this.isPreRelease) &&
        (providedStage == Stage.Auto || providedStage == this.stage) -> {
        nextPreRelease()
    }

    // next stable
    (providedStage == Stage.Auto && this.isNotPreRelease) || providedStage == Stage.Stable -> {
        nextStable(providedModifier.toInc())
    }

    // next stable with next pre-release identifier
    providedStage == Stage.Auto && this.isPreRelease -> {
        nextStableWithPreRelease(providedModifier.toInc(), this.preRelease)
    }

    // next stable with new pre-release identifier
    providedModifier != Modifier.Auto -> {
        newPreRelease(providedModifier.toInc(), providedStage)
    }

    // next patch with new pre-release identifier
    else -> {
        nextPatch("${providedStage.value}.1")
    }
}

fun Version.appendBuildMetadata(buildMetadataOptions: BuildMetadataOptions): Version {
    val calculatedBuildMetadata = LocalDateTime.now().toBuildMetadata()
    val withBuildMetadata = Version(major, minor, patch, preRelease, calculatedBuildMetadata)
    return when (buildMetadataOptions) {
        ALWAYS -> withBuildMetadata
        NEVER -> this
        LOCALLY -> withBuildMetadata.takeIf { System.getenv("CI") == null } ?: this
    }
}

private fun LocalDateTime.toBuildMetadata(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
    return this.format(formatter)
}

private fun Version.nextStable(incrementer: Inc): Version =
    inc(incrementer)

private fun Version.nextStableWithPreRelease(incrementer: Inc, preRelease: String?): Version =
    inc(incrementer, preRelease)

private fun Version.nextSnapshot(incrementer: Inc): Version =
    inc(incrementer, Stage.Snapshot.value)

private fun Version.newPreRelease(incrementer: Inc, stage: Stage): Version =
    inc(incrementer, "${stage.value}.1")

private val Version.stage: Stage?
    get() = Stage.entries.find { preRelease?.contains(it.value, ignoreCase = true) == true }

/**
 * This is different from being stable or not.
 *
 * "stable" means that the major version is greater than 0 AND does not have a pre-release identifier.
 *
 * This just means that the version lacks a pre-release identifier.
 */
val Version.isNotPreRelease: Boolean
    get() = !isPreRelease

/**
 * Current version is invalid for computation when:
 * - A pre-release
 * - The pre-release label is not a valid stage (aka the version has a branch-based pre-release label)
 */
private fun Version.isInvalidVersionForComputation(): Boolean {
    val stages = Stage.entries.map { it.value.lowercase() }
    val prereleaseLabel = preRelease?.substringBefore(".")?.lowercase()

    return isPreRelease && prereleaseLabel !in stages
}
