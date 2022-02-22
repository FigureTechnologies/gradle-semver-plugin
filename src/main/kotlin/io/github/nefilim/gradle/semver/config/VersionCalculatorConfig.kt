package io.github.nefilim.gradle.semver.config

import arrow.core.None
import arrow.core.Option
import io.github.nefilim.gradle.semver.BranchMatchingConfiguration
import io.github.nefilim.gradle.semver.FlowDefaultBranchMatching
import net.swiftzer.semver.SemVer

data class VersionCalculatorConfig(
    val tagPrefix: String,
    val initialVersion: SemVer = SemVer(0, 0, 1),
    val overrideVersion: Option<SemVer> = None,
    val branchMatching: List<BranchMatchingConfiguration> = FlowDefaultBranchMatching { nextPatch() }, // first one matched will apply, put least specific last
) {
    companion object {
        internal val DefaultVersion = SemVer(0, 1, 0, null, null)
        internal const val DefaultTagPrefix = "v"
    }

    fun withBranchMatchingConfig(branchMatching: List<BranchMatchingConfiguration>): VersionCalculatorConfig {
        return this.copy(branchMatching = branchMatching)
    }
}