/**
 * Copyright (c) 2022 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver

import arrow.core.Option
import arrow.core.toOption

internal fun githubActionsBuild(): Boolean = System.getenv("GITHUB_ACTIONS")?.let { it == "true" } ?: false

internal fun pullRequestEvent(): Boolean = System.getenv("GITHUB_EVENT_NAME")?.let { it == "pull_request" } ?: false

internal fun pullRequestHeadRef(): Option<String> = System.getenv("GITHUB_HEAD_REF").toOption()
