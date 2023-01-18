package com.figure.gradle.semver.v1.internal.semver

import net.swiftzer.semver.SemVer

data class VersionCalculatorConfig(
    val tagPrefix: String,
    val initialVersion: SemVer = SemVer(0, 0, 1),
    val overrideVersion: SemVer? = null,
    val branchMatching: VersionCalculatorStrategy = flowVersionCalculatorStrategy { nextPatch() }
) {
    companion object {
        internal val DEFAULT_VERSION = SemVer(0, 1, 0, null, null)
        internal const val DEFAULT_TAG_PREFIX = "v"
    }

    fun withBranchMatchingConfig(branchMatching: VersionCalculatorStrategy): VersionCalculatorConfig {
        return this.copy(branchMatching = branchMatching)
    }
}
