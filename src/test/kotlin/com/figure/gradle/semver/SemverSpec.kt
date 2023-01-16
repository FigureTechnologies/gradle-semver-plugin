/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver

import com.figure.gradle.semver.v0.semverTag
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.WordSpec
import net.swiftzer.semver.SemVer

// Kotest 5 won't work until Gradle moves to 1.6: https://github.com/kotest/kotest/issues/2785
class SemverSpec: WordSpec() {
    init {
        "Semver" should {
            "match valid existing semver tags on refs" {
                "refs/tags/v123".semverTag("v").shouldBeNone()
                "refs/tags/v1.2.3".semverTag("v").shouldBeSome(SemVer(1, 2, 3))
                "refs/tags/v0.1.2-main".semverTag("v").shouldBeSome(SemVer(0, 1, 2, "main"))

                // Semver lib can't understand qualified labels apparently
//                "refs/tags/v0.1.2-main.2".semverTag("v").shouldBeSome(SemVer(0, 1, 2, "main.2"))
//                SemVer.parse("v0.1.2-main.2") shouldBe SemVer(0, 1, 2, "main.2")
            }
        }
    }
}
