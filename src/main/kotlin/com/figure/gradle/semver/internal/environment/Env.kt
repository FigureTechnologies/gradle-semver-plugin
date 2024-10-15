/*
 * Copyright (C) 2024 Figure Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.figure.gradle.semver.internal.environment

object Env {
    const val CI = "CI"
    const val GITHUB_HEAD_REF = "GITHUB_HEAD_REF"
    const val GITHUB_REF_NAME = "GITHUB_REF_NAME"

    val isCI: Boolean
        get() = System.getenv(CI)?.toBoolean() ?: false

    /**
     * The head ref or source branch of the pull request in a workflow run.
     * This property is only set when the event that triggers a workflow run is either pull_request or pull_request_target.
     * For example, feature-branch-1.
     *
     * We want to check to see if this value is set first, because if it isn't, then we know we are not in a pull request.
     * If we are not in a pull request, then we can check the githubRefName.
     */
    val githubHeadRef: String?
        get() = System.getenv(GITHUB_HEAD_REF)

    /**
     * The short ref name of the branch or tag that triggered the workflow run.
     * This value matches the branch or tag name shown on GitHub.
     * For example, feature-branch-1.
     *
     * For pull requests, the format is <pr_number>/merge.
     */
    val githubRefName: String?
        get() = System.getenv(GITHUB_REF_NAME)
}
