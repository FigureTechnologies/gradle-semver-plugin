package com.figure.gradle.semver.v1.internal.semver

import com.figure.gradle.semver.v1.internal.git.GitRef
import com.figure.gradle.semver.v1.internal.git.calculateBaseBranchVersion
import com.figure.gradle.semver.v1.internal.git.currentBranchRef
import com.figure.gradle.semver.v1.internal.git.gitCommitsSinceBranchPoint
import com.figure.gradle.semver.v1.internal.git.headRevInBranch
import com.figure.gradle.semver.v1.internal.git.shortName
import com.figure.gradle.semver.v1.internal.git.tagMap
import net.swiftzer.semver.SemVer
import org.eclipse.jgit.api.Git

interface ContextProviderOperations {
    fun currentBranch(): GitRef.Branch?
    fun branchVersion(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Result<SemVer?>
    fun commitsSinceBranchPoint(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Result<Int?>
}

class GitContextProviderOperations(
    private val git: Git,
    config: VersionCalculatorConfig,
) : ContextProviderOperations {
    private val tags = git.tagMap(config.tagPrefix)

    override fun currentBranch(): GitRef.Branch? {
        return git.currentBranchRef()?.let { ref ->
            ref.shortName().getOrNull()?.let {
                GitRef.Branch(it, ref)
            }
        }
    }

    override fun branchVersion(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Result<SemVer?> {
        return git.calculateBaseBranchVersion(targetBranch, currentBranch, tags)
    }

    override fun commitsSinceBranchPoint(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Result<Int?> {
        return git.headRevInBranch(currentBranch).map { branchPoint ->
            gitCommitsSinceBranchPoint(git, branchPoint, targetBranch, tags).getOrThrow()
        }
    }
}
