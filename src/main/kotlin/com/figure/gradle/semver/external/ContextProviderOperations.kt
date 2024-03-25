/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.external

import com.figure.gradle.semver.internal.git.GitRef
import net.swiftzer.semver.SemVer

interface ContextProviderOperations {
    fun currentBranch(): GitRef.Branch?

    fun branchVersion(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Result<SemVer?>

    fun commitsSinceBranchPoint(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Result<Int?>
}
