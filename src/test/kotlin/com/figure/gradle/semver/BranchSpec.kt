/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver

import com.figure.gradle.semver.domain.GitRef
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class BranchSpec: WordSpec() {
    init {
        "Branch" should {
            "sanitize names correctly" {
                GitRef.Branch("feature/something.5-bla\$bla").sanitizedName() shouldBe "feature-something.5-bla-bla"
                GitRef.Branch("feature/something.5-bla\$bla").sanitizedNameWithoutPrefix() shouldBe "something.5-bla-bla"
                GitRef.Branch("feature/something_other.5").sanitizedNameWithoutPrefix() shouldBe "something_other.5"
                GitRef.Branch("something_other.5").sanitizedNameWithoutPrefix() shouldBe "something_other.5"
            }
        }
    }
}
