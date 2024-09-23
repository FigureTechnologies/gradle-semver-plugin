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
package com.figure.gradle.semver.git

import com.figure.gradle.semver.internal.command.KGit

class GitInstanceWriter(
    private val localGit: KGit,
    private val gitActionsConfig: GitActionsConfig,
) {
    fun write(printGitObjects: Boolean) {
        gitActionsConfig.actions
            .filterNot { it is RemovalAction }
            .forEach { action -> action.execute(localGit) }

        localGit.push.all()

        // This will run any removal actions
        gitActionsConfig.actions
            .filterIsInstance<RemovalAction>()
            .forEach { action -> action.execute(localGit) }

        localGit.print.commits(printGitObjects)
        localGit.print.refs(printGitObjects)
        localGit.print.tags(printGitObjects)
    }
}
