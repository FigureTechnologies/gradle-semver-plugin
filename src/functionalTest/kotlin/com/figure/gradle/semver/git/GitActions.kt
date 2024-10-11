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
import com.figure.gradle.semver.projects.AbstractProject
import com.figure.gradle.semver.util.resolveResource
import org.eclipse.jgit.lib.Constants
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.io.BufferedReader
import java.io.InputStreamReader

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

sealed interface Action {
    fun execute(git: KGit)
}

sealed interface RemovalAction : Action

data class CheckoutAction(
    val branch: String,
) : Action {
    override fun execute(git: KGit) {
        if (git.branch.headRef.objectId?.name == null) {
            git.commit("Initial commit", allowEmptyCommit = true)
        }

        if (git.branches.exists(branch)) {
            git.checkout(branch)
        } else {
            git.checkout(branch, createBranch = true)
        }
    }
}

data class CommitAction(
    val message: String = "Empty Commit",
    val tag: String? = null,
) : Action {
    override fun execute(git: KGit) {
        git.commit(message, true)

        if (!tag.isNullOrBlank()) {
            git.tag("v$tag")
        }
    }
}

data class RunScriptAction(
    val script: Script,
    val project: AbstractProject,
    val arguments: List<String>,
) : Action {
    override fun execute(git: KGit) {
        val scriptFile = resolveResource("scripts/${script.scriptFileName}")
        val projectPath = project.gradleProject.rootDir.absolutePath

        val processBuilder = ProcessBuilder()
        processBuilder.command("bash", scriptFile.absolutePath, projectPath, *arguments.toTypedArray())
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()

        log.lifecycle("\nScript output:\n")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.useLines { lines ->
            lines.forEach { log.lifecycle(it) }
        }
        log.lifecycle("\nEnd script output\n")

        process.waitFor()
    }
}

data class RemoveLocalBranchAction(
    val branch: String,
) : RemovalAction {
    override fun execute(git: KGit) {
        git.branch.delete("${Constants.R_HEADS}$branch")
    }
}

data class RemoveRemoteBranchAction(
    val branch: String,
) : RemovalAction {
    override fun execute(git: KGit) {
        git.branch.delete("${Constants.R_REMOTES}${Constants.DEFAULT_REMOTE_NAME}/$branch")
    }
}

class GitActionsConfig(
    private val project: AbstractProject,
) {
    val actions = mutableListOf<Action>()

    fun actions(config: Actions.() -> Unit) {
        val actionObject = Actions(project)
        actionObject.config()
        actions.addAll(actionObject.actionsToRun)
    }
}

class Actions(
    private val project: AbstractProject,
) {
    internal val actionsToRun = mutableListOf<Action>()

    fun checkout(branch: String) {
        val checkoutAction = CheckoutAction(branch)
        actionsToRun.add(checkoutAction)
    }

    fun commit(message: String = "Empty Commit", tag: String = "") {
        val commitAction = CommitAction(message, tag)
        actionsToRun.add(commitAction)
    }

    fun runScript(script: Script, vararg arguments: String) {
        val runScriptAction = RunScriptAction(script, project, arguments.toList())
        actionsToRun.add(runScriptAction)
    }

    fun removeLocalBranch(branch: String) {
        val removeLocalBranchAction = RemoveLocalBranchAction(branch)
        actionsToRun.add(removeLocalBranchAction)
    }

    fun removeRemoteBranch(branch: String) {
        val removeRemoteBranchAction = RemoveRemoteBranchAction(branch)
        actionsToRun.add(removeRemoteBranchAction)
    }
}

enum class Script(
    val scriptFileName: String,
) {
    CREATE_BISECTING_STATE("create_bisecting_state.sh"),
    CREATE_CHERRY_PICKING_STATE("create_cherry_picking_state.sh"),
    CREATE_DETACHED_HEAD_STATE("create_detached_head_state.sh"),
    CREATE_MERGING_STATE("create_merging_state.sh"),
    CREATE_REBASING_STATE("create_rebasing_state.sh"),
    CREATE_REVERTING_STATE("create_reverting_state.sh"),
}
