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
package com.figure.gradle.semver.internal.command

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants

// This is heavily influenced by the gitstatus logic found here:
// https://github.com/magicmonty/bash-git-prompt/
// TODO: Future consideration: Support rebasing step in the version. So something like:
//  - `1.2.3-rebasing.2-12` - would mean step 2 out of 12 of rebasing.
class State(
    private val git: Git,
) {
    operator fun invoke(): GitState =
        when {
            rebasing -> GitState.REBASING
            merging -> GitState.MERGING
            cherryPicking -> GitState.CHERRY_PICKING
            reverting -> GitState.REVERTING
            bisecting -> GitState.BISECTING
            detachedHead -> GitState.DETACHED_HEAD
            else -> GitState.NOMINAL
        }

    private val rebasing: Boolean
        get() = git.repository.directory.resolve("rebase-merge").exists()

    private val merging: Boolean
        get() = git.repository.directory.resolve("MERGE_HEAD").exists()

    private val cherryPicking: Boolean
        get() = git.repository.directory.resolve("CHERRY_PICK_HEAD").exists()

    private val reverting: Boolean
        get() = git.repository.directory.resolve("REVERT_HEAD").exists()

    private val bisecting: Boolean
        get() = git.repository.directory.resolve("BISECT_LOG").exists()

    // At least for GitHub actions, an on-push event will cause a detached head state
    // However, we have information about the branch we're actually building so it is an
    // exception to this and not something we consider non-nominal
    private val detachedHead: Boolean
        get() = git.repository.exactRef(Constants.HEAD).target.objectId.name == git.repository.branch && System.getenv("CI") == null
}

enum class GitState(val description: String) {
    NOMINAL(""),
    BISECTING("BISECTING"),
    CHERRY_PICKING("CHERRY-PICKING"),
    DETACHED_HEAD("DETACHED-HEAD"),
    MERGING("MERGING"),
    REBASING("REBASING"),
    REVERTING("REVERTING"),
}
