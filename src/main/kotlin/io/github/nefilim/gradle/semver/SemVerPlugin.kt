package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.getOrElse
import arrow.core.getOrHandle
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import com.javiersc.semver.Version
import io.github.nefilim.gradle.semver.SemVerExtension.Companion.semver
import org.eclipse.jgit.api.ListBranchCommand
import org.gradle.api.Plugin
import org.gradle.api.Project

public class SemVerPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        val semVerExtension = target.semver()

        if (target.hasGit) {
            target.afterEvaluate {
                val config = semVerExtension.buildPluginConfig()
                target.logger.lifecycle("semver configuration $config")
                val context = SemVerPluginContext(target.git, config, target)

                target.version = config.overrideVersion.getOrElse {
                    context.calculateVersionFlow().getOrHandle {
                        target.logger.lifecycle("failed to calculate version: $it")
                        throw Exception("$it")
                    }
                }
                context.generateVersionFile()

                if (target == target.rootProject)
                    target.allprojects { it.project.version = target.version }

                target.gradle.projectsEvaluated {
                    if (target.appliedOnlyOnRootProject)
                        target.semverMessage("semver: ${target.version}")
                    else
                        target.semverMessage("semver for ${target.name}: ${target.version}")
                }
            }
        } else {
            target.semverMessage("the current directory is not part of a git repo, cannot determine project semantic version number, please initialize a git repo with main & develop branches")
        }
    }
}

private fun SemVerPluginContext.calculateVersionFlow(): Either<SemVerError, Version> {
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
                project.logger.lifecycle("found main: $mainRefName, develop: $developRefName")
                val main = git.buildBranch(mainRefName, config).bind() as GitRef.MainBranch
                val develop = git.buildBranch(developRefName, config).bind() as GitRef.DevelopBranch
                val current = git.buildBranch(repository.fullBranch, config).bind()
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
    project.logger.warn("""
        |could not find [$branchName] branch, defaulting to initial version: ${config.initialVersion}
        | please create the following required branches:
        |   main
        |     ∟ develop
        """.trimMargin())
}