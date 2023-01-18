package com.figure.gradle.semver.v1.internal

internal fun githubActionsBuild(): Boolean =
    System.getenv("GITHUB_ACTIONS")
        ?.let { it == "true" } ?: false

internal fun pullRequestEvent(): Boolean =
    System.getenv("GITHUB_EVENT_NAME")
        ?.let { it == "pull_request" } ?: false

internal fun pullRequestHeadRef(): String? =
    System.getenv("GITHUB_HEAD_REF")
