/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.util

import io.kotest.core.config.AbstractProjectConfig

class KotestProjectConfig : AbstractProjectConfig() {
    override suspend fun beforeProject() {
        System.setProperty("KOTEST_TEST", "true")
    }

    override suspend fun afterProject() {
        System.clearProperty("KOTEST_TEST")
    }
}
