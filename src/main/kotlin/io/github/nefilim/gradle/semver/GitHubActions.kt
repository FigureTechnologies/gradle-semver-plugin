package io.github.nefilim.gradle.semver

import arrow.core.Option
import arrow.core.toOption

internal fun githubActionsBuild(): Boolean = System.getenv("GITHUB_ACTIONS")?.let { it == "true" } ?: false

internal fun pullRequestEvent(): Boolean = System.getenv("GITHUB_EVENT_NAME")?.let { it == "pull_request" } ?: false

internal fun pullRequestHeadRef(): Option<String> = System.getenv("GITHUB_HEAD_REF").toOption()