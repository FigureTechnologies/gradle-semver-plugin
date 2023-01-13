/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.domain

import java.io.PrintWriter
import java.io.StringWriter

sealed interface SemverError {
    data class Git(val t: Throwable): SemverError
    data class UnsupportedBranch(val ref: String): SemverError
    data class Unexpected(val message: String): SemverError
    data class MissingVersion(val message: String): SemverError
    data class MissingRef(val message: String): SemverError
    data class MissingBranchMatchingConfiguration(val currentBranch: GitRef.Branch): SemverError
    data class MissingConfiguration(val message: String): SemverError

}

fun SemverError.toError(): String {
    return when (this) {
        is SemverError.Git -> {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            this.t.printStackTrace(pw)
            sw.toString()
        }
        else -> this.toString()
    }
}
