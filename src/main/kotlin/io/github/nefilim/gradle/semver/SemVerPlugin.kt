package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.computations.either
import com.javiersc.semver.Version
import io.github.nefilim.gradle.semver.SemVerExtension.Companion.semver
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.GitRef.Companion.RemoteOrigin
import io.github.nefilim.gradle.semver.domain.SemVerError
import org.eclipse.jgit.api.ListBranchCommand
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import java.io.File

public class SemVerPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.semver()
        if (!target.hasGit)
            target.logger.warn("the current directory is not part of a git repo, cannot determine project semantic version number, please initialize a git repo with main & develop branches")

        target.tasks.register("cv", CurrentVersionTask::class.java)
        target.tasks.register("generateVersionFile", GenerateVersionFileTask::class.java)
    }
}

open class CurrentVersionTask: DefaultTask() {
    @TaskAction
    fun currentVersion() {
        project.logger.lifecycle("version: ${(project.extensions[SemVerExtension.ExtensionName] as SemVerExtension).version()}".purple())
    }
}

open class GenerateVersionFileTask: DefaultTask() {
    @TaskAction
    fun generateVersionFile() {
        val extension = (project.extensions[SemVerExtension.ExtensionName] as SemVerExtension)
        with (project) {
            File("$buildDir/semver/version.txt").apply {
                parentFile.mkdirs()
                createNewFile()
                writeText(
                    """
                       |${extension.version().value}
                       |${extension.versionTagName()}
                    """.trimMargin()
                )
            }
        }
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
                // if we're running under GitHub Actions and this is a PR event, we're in detached HEAD state, not on a branch
                val current = if (githubActionsBuild() && pullRequestEvent()) {
                    log("we're running under Github Actions during a PR event")
                    val headRef = pullRequestHeadRef().map { "$RemoteOrigin/$it" }.toEither { SemVerError.MissingRef("failed to find GITHUB_HEAD_REF for a pull request event??") }.bind()
                    log("using $headRef as branch")
                    buildBranch(headRef, config).bind()
                } else
                    buildBranch(repository.fullBranch, config).bind()

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