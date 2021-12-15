package io.github.nefilim.gradle.semver.domain

import io.github.nefilim.gradle.semver.config.Scope

sealed interface SemVerError {
    data class Git(val t: Throwable): SemVerError
    data class UnsupportedBranch(val ref: String): SemVerError
    data class UnsupportedScope(val s: Scope): SemVerError
    data class Unexpected(val message: String): SemVerError
    data class MissingVersion(val message: String): SemVerError
}