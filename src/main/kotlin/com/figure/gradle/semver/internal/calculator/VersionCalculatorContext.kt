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
package com.figure.gradle.semver.internal.calculator

import com.figure.gradle.semver.internal.command.GitState
import com.figure.gradle.semver.internal.properties.BuildMetadataOptions
import com.figure.gradle.semver.internal.properties.Modifier
import com.figure.gradle.semver.internal.properties.Stage

data class VersionCalculatorContext(
    val stage: Stage,
    val modifier: Modifier,
    val forTesting: Boolean,
    val gitState: GitState,
    val mainBranch: String? = null,
    val developmentBranch: String? = null,
    val appendBuildMetadata: BuildMetadataOptions,
)
