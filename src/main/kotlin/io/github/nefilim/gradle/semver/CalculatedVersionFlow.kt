package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.config.Stage
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import com.javiersc.semver.Version
import com.javiersc.semver.Version.Increase

@Suppress("ComplexMethod")
internal fun SemVerPluginContext.calculatedVersionFlow(
    main: GitRef.MainBranch,
    develop: GitRef.DevelopBranch,
    currentBranch: GitRef.Branch,
): Either<SemVerError, Version> {
    val tags = git.tagMap(config.tagPrefix)
    println("calculating version with currentBranch $currentBranch")
    return when (currentBranch) {
        is GitRef.MainBranch -> {
            main.version.fold({
                project.semverMessage("unable to determine last version, using initialVersion [${config.initialVersion}]")
                config.initialVersion.right()
            },{
                applyScopeToVersion(it, main.scope, main.stage)
            })
        }
        is GitRef.DevelopBranch -> {
            // recalculate version automatically based on releases on main
            either.eager {
                val branchPoint = git.headRevInBranch(main).bind()
                val commitCount = git.commitsSinceBranchPoint(branchPoint, main).bind()
                git.calculateDevelopBranchVersion(config, main, develop, tags).map {
                    it.getOrElse {
                        println("COULDNT CALC DEV VERSION")
                        project.semverMessage("unable to determine last version, using initialVersion [${config.initialVersion}]")
                        config.initialVersion
                    }.copy(stageNum = commitCount)
                }.bind()
            }
        }
        is GitRef.FeatureBranch -> {
            // must have been branched from develop
            either.eager {
                val devVersion = git.calculateDevelopBranchVersion(config, main, develop, tags).bind()
                val branchPoint = git.headRevInBranch(develop).bind()
                val commitCount = git.commitsSinceBranchPoint(branchPoint, develop).bind()
                devVersion.fold({
                    SemVerError.MissingVersion("unable to find version tag on develop").left()
                }, {
                    applyScopeToVersion(it, currentBranch.scope, currentBranch.stage).map { it.copy(stageNum = commitCount) }
                }).bind()
            }
        }
        is GitRef.HotfixBranch -> {
            // must have been branched from main
            either.eager {
                val devVersion = git.calculateDevelopBranchVersion(config, main, develop, tags).bind()
                val branchPoint = git.headRevInBranch(main).bind()
                val commitCount = git.commitsSinceBranchPoint(branchPoint, main).bind()
                devVersion.fold({
                    SemVerError.MissingVersion("unable to find version tag on main").left()
                }, {
                    applyScopeToVersion(it, currentBranch.scope, currentBranch.stage).map { it.copy(stageNum = commitCount) }
                }).bind()
            }
        }
        else -> {
            SemVerError.UnsupportedBranch(currentBranch.refName).left()
        }
    }
}

internal fun applyScopeToVersion(version: Version, scope: Scope, stage: Stage? = null): Either<SemVerError, Version> {
    return when (scope) {
        Scope.Major -> version.inc(Increase.Major, stage.toStageName()).right()
        Scope.Minor -> version.inc(Increase.Minor, stage.toStageName()).right()
        Scope.Patch -> version.inc(Increase.Patch, stage.toStageName()).right()
        Scope.Auto -> {
            SemVerError.UnsupportedScope(scope).left()
        }
    }
}

// TODO create an ADT for Stage so we can derive string name here
internal fun Stage?.toStageName(): String {
    return when (this) {
        null -> ""
        Stage.Final -> ""
        else -> this.name.lowercase()
    }
}