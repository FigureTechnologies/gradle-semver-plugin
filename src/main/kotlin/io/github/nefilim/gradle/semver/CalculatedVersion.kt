package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.computations.either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.config.Stage
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import org.eclipse.jgit.api.Git
import net.swiftzer.semver.SemVer

interface VersionCalculationOperations {
    fun calculateBaseBranchVersion(
        baseBranch: GitRef.Branch,
        currentBranch: GitRef.Branch,
    ): Either<SemVerError, Option<SemVer>>

    fun qualifyStage(
        version: SemVer,
        baseBranch: GitRef.Branch,
        currentBranch: GitRef.Branch,
    ): Either<SemVerError, SemVer>
}

fun SemVerPluginContext.getGitVersionCalculationOperations(git: Git, tags: Tags): VersionCalculationOperations = object: VersionCalculationOperations {
    override fun calculateBaseBranchVersion(baseBranch: GitRef.Branch, currentBranch: GitRef.Branch): Either<SemVerError, Option<SemVer>> {
        return git.calculateBaseBranchVersion(baseBranch, currentBranch, tags)
    }

    override fun qualifyStage(
        version: SemVer,
        baseBranch: GitRef.Branch,
        currentBranch: GitRef.Branch,
    ): Either<SemVerError, SemVer> {
        return either.eager<SemVerError, Int> {
            val branchPoint = git.headRevInBranch(baseBranch).bind()
            commitsSinceBranchPoint(git, branchPoint, currentBranch, tags).bind()
        }.map { count ->
            version.copy(preRelease = "${version.preRelease}.$count")
        }
    }
}

internal fun SemVerPluginContext.calculatedVersionFlow(
    main: GitRef.MainBranch,
    develop: GitRef.DevelopBranch,
    currentBranch: GitRef.Branch,
    ops: VersionCalculationOperations,
): Either<SemVerError, SemVer> {
    verbose("calculating version for current branch $currentBranch, main: $main, develop: $develop")
    return when (currentBranch) {
        is GitRef.MainBranch -> {
            calculateCurrentBranchVersion(main, currentBranch, ops, main.version.right(), config.initialVersion.some())
        }
        is GitRef.DevelopBranch -> {
            // recalculate version automatically based on releases on main
            calculateCurrentBranchVersion(main, currentBranch, ops, fallbackVersion = config.initialVersion.some())
        }
        is GitRef.FeatureBranch -> {
            // must have been branched from develop
            calculateCurrentBranchVersion(develop, currentBranch, ops)
        }
        is GitRef.HotfixBranch -> {
            // must have been branched from main
            calculateCurrentBranchVersion(main, currentBranch, ops)
        }
    }
}

internal fun SemVerPluginContext.calculatedVersionFlat(
    main: GitRef.MainBranch,
    currentBranch: GitRef.Branch,
    ops: VersionCalculationOperations,
): Either<SemVerError, SemVer> {
    verbose("calculating flat version for current branch $currentBranch, main: $main")
    return when (currentBranch) {
        is GitRef.MainBranch -> {
            calculateCurrentBranchVersion(main, currentBranch, ops, main.version.right(), config.initialVersion.some())
        }
        else -> {
            // recalculate version automatically based on releases on main
            calculateCurrentBranchVersion(main, currentBranch, ops)
        }
    }
}

private fun SemVerPluginContext.calculateCurrentBranchVersion(
    baseBranch: GitRef.Branch,
    currentBranch: GitRef.Branch,
    ops: VersionCalculationOperations,
    baseBranchVersion: Either<SemVerError, Option<SemVer>> = ops.calculateBaseBranchVersion(baseBranch, currentBranch),
    fallbackVersion: Option<SemVer> = None,
): Either<SemVerError, SemVer> {
    return either.eager {
        val baseBranchSemver = baseBranchVersion.bind()
        fallbackVersion.fold({
            baseBranchSemver.fold({
                SemVerError.MissingVersion("unable to find version tag on [$baseBranch], [$currentBranch] must be branched from ${baseBranch.name}").left()
            }, {
                calculateNextVersion(baseBranch, currentBranch, it, ops)
            })
        }, { fallbackSemver ->
            baseBranchSemver.getOrElse {
                warn("unable to determine last version from base branch [$baseBranch], using [$fallbackSemver] as base version")
                fallbackSemver
            }.let {
                calculateNextVersion(baseBranch, currentBranch, it, ops)
            }
        }).bind()
    }
}

internal fun calculateNextVersion(
    baseBranch: GitRef.Branch,
    currentBranch: GitRef.Branch,
    baseBranchVersion: SemVer,
    ops: VersionCalculationOperations,
): Either<SemVerError, SemVer> {
    return when (currentBranch.scope) {
        Scope.Major -> baseBranchVersion.nextMajor()
        Scope.Minor -> baseBranchVersion.nextMinor()
        Scope.Patch -> baseBranchVersion.nextPatch()
    }.let {
        when (currentBranch.stage) {
            Stage.Final -> it.right() // Final does not have a prerelease label or need to be qualified
            Stage.Snapshot -> it.copy(preRelease = currentBranch.stage.toStageName(currentBranch)).right() // Snapshot does not need to be qualified
            else -> ops.qualifyStage(it.copy(preRelease = currentBranch.stage.toStageName(currentBranch)), baseBranch, currentBranch)
        }
    }
}

internal fun Stage.toStageName(currentBranch: GitRef.Branch): String? {
    return when (this) {
        Stage.Final -> null
        Stage.Snapshot -> "SNAPSHOT"
        Stage.Branch -> currentBranch.name.substringAfterLast('/')
        else -> this.name.lowercase()
    }
}