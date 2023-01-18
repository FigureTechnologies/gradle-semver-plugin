package com.figure.gradle.semver.v1.internal.semver

@JvmInline
value class PreReleaseLabel(val value: String) {
    companion object {
        val EMPTY = PreReleaseLabel("")
    }
}

@JvmInline
value class BuildMetadataLabel(val value: String) {
    companion object {
        val EMPTY = BuildMetadataLabel("")
    }
}


