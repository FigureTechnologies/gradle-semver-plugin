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
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.URIish
import java.net.URL

class Remote(
    private val git: Git,
) {
    fun add(
        remoteGit: Git,
        remoteName: String = Constants.DEFAULT_REMOTE_NAME,
    ): RemoteConfig =
        git.remoteAdd()
            .setName(remoteName)
            .setUri(URIish(remoteGit.repository.directory.toURI().toURL()))
            .call()

    fun add(
        remoteUri: String,
        remoteName: String = Constants.DEFAULT_REMOTE_NAME,
    ): RemoteConfig =
        git.remoteAdd()
            .setName(remoteName)
            .setUri(URIish(remoteUri))
            .call()

    fun add(
        remoteUri: URL,
        remoteName: String = Constants.DEFAULT_REMOTE_NAME,
    ): RemoteConfig =
        git.remoteAdd()
            .setName(remoteName)
            .setUri(URIish(remoteUri))
            .call()
}
