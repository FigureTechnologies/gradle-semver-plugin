package io.github.nefilim.gradle.semver.domain

sealed interface SemVerError {
    data class Git(val t: Throwable): SemVerError
    data class UnsupportedBranch(val ref: String): SemVerError
    data class Unexpected(val message: String): SemVerError
    data class MissingVersion(val message: String): SemVerError
    data class MissingRef(val message: String): SemVerError
    data class MissingBranchMatchingConfiguration(val currentBranch: GitRef.Branch): SemVerError
    data class MissingConfiguration(val message: String): SemVerError
}