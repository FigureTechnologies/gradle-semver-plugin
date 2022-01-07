package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.computations.either
import com.javiersc.semver.Version
import io.github.nefilim.gradle.semver.SemVerExtension.Companion.semver
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import org.eclipse.jgit.api.ListBranchCommand
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get

public class SemVerPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.semver()
        if (!target.hasGit)
            target.logger.warn("the current directory is not part of a git repo, cannot determine project semantic version number, please initialize a git repo with main & develop branches")

        target.tasks.register("cv", CurrentVersionTask::class.java)
    }
}

open class CurrentVersionTask: DefaultTask() {
    @TaskAction
    fun currentVersion() {
        project.logger.lifecycle("version: ${(project.extensions[SemVerExtension.ExtensionName] as SemVerExtension).version()}".purple())
    }
}

internal fun SemVerPluginContext.calculateVersionFlow(): Either<SemVerError, Version> {
    verbose("current branch: ${repository.fullBranch}")

    return either.eager {
        val allBranches = Either.catch { git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().toList() }.mapLeft { SemVerError.Git(it) }.bind()
        val mainRefName = allBranches.firstOrNull { setOf(GitRef.MainBranch.RefName, GitRef.MainBranch.RemoteOriginRefName).contains(it.name) }?.name
        val developRefName = allBranches.firstOrNull { setOf(GitRef.DevelopBranch.RefName, GitRef.DevelopBranch.RemoteOriginRefName).contains(it.name) }?.name
        when {
            mainRefName == null -> {
                missingRequiredBranch(GitRef.MainBranch.Name)
                config.initialVersion
            }
            developRefName == null -> {
                missingRequiredBranch(GitRef.DevelopBranch.Name)
                config.initialVersion
            }
            else -> {
                verbose("found main: $mainRefName, develop: $developRefName")
                val main = buildBranch(mainRefName, config).bind() as GitRef.MainBranch
                val develop = buildBranch(developRefName, config).bind() as GitRef.DevelopBranch
                val current = buildBranch(repository.fullBranch, config).bind()
                calculatedVersionFlow(
                    main,
                    develop,
                    current,
                ).bind()
            }
        }
    }
}

private fun SemVerPluginContext.missingRequiredBranch(branchName: String) {
    warn("""
        |could not find [$branchName] branch, defaulting to initial version: ${config.initialVersion}, please create the following required branches:
        | main
        |   ∟ develop
        """.trimMargin())
}