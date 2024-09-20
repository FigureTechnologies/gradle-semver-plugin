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
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RefSpec

class Push(
    private val git: Git,
) {
    operator fun invoke(): MutableIterable<PushResult>? =
        git.push().call()

    fun branch(branch: String): MutableIterable<PushResult>? =
        git.push()
            .setRefSpecs(RefSpec("${Constants.R_HEADS}$branch:${Constants.R_HEADS}$branch"))
            .call()

    fun tag(tag: String): MutableIterable<PushResult>? =
        git.push()
            .setRefSpecs(RefSpec("${Constants.R_TAGS}$tag:${Constants.R_TAGS}$$tag"))
            .call()

    fun allBranches(): MutableIterable<PushResult>? =
        git.push()
            .setPushAll() // Push all branches under refs/heads/*
            .call()

    fun allTags(): MutableIterable<PushResult>? =
        git.push()
            .setPushTags() // Push all tags under refs/tags/*
            .call()

    fun all(): MutableIterable<PushResult>? =
        git.push()
            .setPushAll() // Push all branches under refs/heads/*
            .setPushTags() // Push all tags under refs/tags/*
            .call()
}
