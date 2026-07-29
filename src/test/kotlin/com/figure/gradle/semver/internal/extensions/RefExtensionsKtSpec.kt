/*
 * Copyright (C) 2025 Figure Technologies
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
package com.figure.gradle.semver.internal.extensions

import io.github.z4kn4fein.semver.nextPreRelease
import io.github.z4kn4fein.semver.toVersion
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Ref.Storage

class RefExtensionsKtSpec : FunSpec({
    test("prereleaseLabel for dependabot branch") {
        // Create a test implementation of Ref
        val branchName = "dependabot-github_actions-softprops-action-gh-release-2"
        val fullRefName = Constants.R_HEADS + branchName

        // Create a mock Ref with the specified branch name
        val ref = object : Ref {
            override fun getName(): String = fullRefName

            override fun getObjectId(): ObjectId? = null

            override fun getPeeledObjectId(): ObjectId? = null

            override fun isPeeled(): Boolean = false

            override fun getStorage(): Storage = Storage.LOOSE

            override fun isSymbolic(): Boolean = false

            override fun getTarget(): Ref? = null

            override fun getLeaf(): Ref = this
        }

        // Test the prereleaseLabel extension function
        val expectedLabel = "dependabot-github-actions-softprops-action-gh-release-2"
        ref.prereleaseLabel() shouldBe expectedLabel

        // Test the prereleaseLabel extension function produces valid preRelease values
        val expectedVersion = "1.0.1-$expectedLabel".toVersion(true)
        "1.0.0".toVersion(true).nextPreRelease(expectedLabel) shouldBe expectedVersion
    }
})
