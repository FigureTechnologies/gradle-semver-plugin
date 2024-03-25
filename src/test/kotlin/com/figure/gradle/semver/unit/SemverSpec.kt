/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.unit

import com.figure.gradle.semver.internal.git.semverTag
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import net.swiftzer.semver.SemVer

class SemverSpec : WordSpec({
    "Semver" should {
        "match valid existing semver tags on refs" {
            "refs/tags/v123".semverTag("v") should beNull()
            "refs/tags/v1.2.3".semverTag("v") shouldBe SemVer(1, 2, 3)
            "refs/tags/v0.1.2-main".semverTag("v") shouldBe SemVer(0, 1, 2, "main")

            // Semver lib can't understand qualified labels apparently
            // "refs/tags/v0.1.2-main.2".semverTag("v") shouldBe SemVer(0, 1, 2, "main.2")
            // SemVer.parse("v0.1.2-main.2") shouldBe SemVer(0, 1, 2, "main.2")
        }
    }
})
