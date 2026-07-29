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
package com.figure.gradle.semver.specs

import com.figure.gradle.semver.kotest.GradleProjectsExtension
import com.figure.gradle.semver.kotest.shouldOnlyHave
import com.figure.gradle.semver.projects.RegularProject
import com.figure.gradle.semver.projects.SettingsProject
import com.figure.gradle.semver.projects.SubprojectProject
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import org.gradle.util.GradleVersion

class UninitializedRepositorySpec : FunSpec({
    val projects = install(
        GradleProjectsExtension(
            RegularProject(projectName = "regular-project"),
            SettingsProject(projectName = "settings-project"),
            SubprojectProject(projectName = "subproject-project"),
        ),
    )

    test("should build on uninitialized repository") {
        // When
        projects.build(GradleVersion.current())

        // Then
        // This assumes that the initial version is 0.0.0 and that the first build will generate a patch version
        projects.versions shouldOnlyHave "0.0.1-UNINITIALIZED-REPO"
    }
})
