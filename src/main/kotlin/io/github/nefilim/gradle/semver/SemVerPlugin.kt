package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.getOrHandle
import io.github.nefilim.gradle.semver.config.PluginConfig
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import com.javiersc.semver.Version
import org.gradle.api.Plugin
import org.gradle.api.Project

public class SemVerPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        if (target.hasGit) {

            val config = PluginConfig.fromProjectProperties(target)
            val context = SemVerPluginContext(target.git, config, target)

            target.version = context.calculateVersionFlow().getOrHandle {
                target.logger.lifecycle("failed to calculate version: $it")
                throw Exception("$it")
            }
            context.generateVersionFile()

            if (target == target.rootProject) {
                target.allprojects { it.project.version = target.version }
            }

            target.gradle.projectsEvaluated {
                if (target.appliedOnlyOnRootProject)
                    target.semverMessage("semver: ${target.version}")
                else
                    target.semverMessage("semver for ${target.name}: ${target.version}")
            }
        } else {
            target.semverMessage("semver plugin can't work if the project is not a git repository")
        }
    }
}

private fun SemVerPluginContext.calculateVersionFlow(): Either<SemVerError, Version> {
    return either.eager {
        val main = git.buildBranch(GitRef.MainBranch.RefName, config).bind() as GitRef.MainBranch
        val develop = git.buildBranch(GitRef.DevelopBranch.RefName, config).bind() as GitRef.DevelopBranch
        val current = git.buildBranch(repository.fullBranch, config).bind()
        calculatedVersionFlow(
            main,
            develop,
            current,
        ).bind().also { println("CALCULATED VERSION: $it") }
    }
}