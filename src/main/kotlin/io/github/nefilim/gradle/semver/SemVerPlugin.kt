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
                val context = SemVerPluginContext(target.git, config, target)
                context.verbose("semver configuration: $config")

                val calculatedVersion = config.overrideVersion.getOrElse {
                    context.calculateVersionFlow().getOrHandle {
                        context.error("failed to calculate version: $it".red())
                        throw Exception("$it")
                    }
                }
                target.version = calculatedVersion.toString()
                semVerExtension.setVersion(calculatedVersion)
                context.generateVersionFile()

                if (target == target.rootProject)
                    target.allprojects { it.project.version = target.version }

                target.gradle.projectsEvaluated {
                    if (target.appliedOnlyOnRootProject)
                        context.verbose("semver: ${target.version}".purple())
                    else
                        context.verbose("semver for ${target.name}: ${target.version}".purple())
                }
            }
        } else {
            target.logger.warn("the current directory is not part of a git repo, cannot determine project semantic version number, please initialize a git repo with main & develop branches")
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
                verbose("found main: $mainRefName, develop: $developRefName")
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
    warn("""
        |could not find [$branchName] branch, defaulting to initial version: ${config.initialVersion}, please create the following required branches:
        | main
        |   ∟ develop
        """.trimMargin())
}