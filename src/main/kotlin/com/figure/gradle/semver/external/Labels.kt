/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.external

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


