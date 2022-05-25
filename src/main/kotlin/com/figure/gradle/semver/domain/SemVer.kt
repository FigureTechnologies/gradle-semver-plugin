package com.figure.gradle.semver.domain

import java.io.PrintWriter
import java.io.StringWriter

sealed interface SemVerError {
    data class Git(val t: Throwable): SemVerError
    data class UnsupportedBranch(val ref: String): SemVerError
    data class Unexpected(val message: String): SemVerError
    data class MissingVersion(val message: String): SemVerError
    data class MissingRef(val message: String): SemVerError
    data class MissingBranchMatchingConfiguration(val currentBranch: GitRef.Branch): SemVerError
    data class MissingConfiguration(val message: String): SemVerError

}

fun SemVerError.toError(): String {
    return when (this) {
        is SemVerError.Git -> {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            this.t.printStackTrace(pw)
            sw.toString()
        }
        else -> this.toString()
    }
}