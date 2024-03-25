/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.semver

import com.figure.gradle.semver.external.ContextProviderOperations
import com.figure.gradle.semver.external.SemverContext

internal class GradleSemverContext(
    override val ops: ContextProviderOperations,
) : SemverContext
