/*
 * Copyright (C) 2024 Figure Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.figure.gradle.semver.kotest

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.system.OverrideMode

class KotestProjectConfig : AbstractProjectConfig() {
    // Tells Kotest that it's never running in CI
    // If we need tests specifically for CI, we'll need to manually add it via withEnvironment
    override fun extensions(): List<Extension> =
        listOf(SystemEnvironmentProjectListener(mapOf("CI" to null), OverrideMode.SetOrOverride))
}
