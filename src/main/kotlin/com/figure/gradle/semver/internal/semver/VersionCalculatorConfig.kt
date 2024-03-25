/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.semver

import com.figure.gradle.semver.external.VersionCalculatorStrategy
import com.figure.gradle.semver.external.mainBasedFlowVersionCalculatorStrategy
import net.swiftzer.semver.SemVer

internal data class VersionCalculatorConfig(
    val tagPrefix: String,
    val initialVersion: SemVer = SemVer(0, 0, 1),
    val overrideVersion: SemVer? = null,
    val branchMatching: VersionCalculatorStrategy = mainBasedFlowVersionCalculatorStrategy { nextPatch() }
) {
    companion object {
        internal val DEFAULT_VERSION = SemVer(0, 1, 0, null, null)
        internal const val DEFAULT_TAG_PREFIX = "v"
    }

    fun withBranchMatchingConfig(branchMatching: VersionCalculatorStrategy): VersionCalculatorConfig {
        return this.copy(branchMatching = branchMatching)
    }
}
