package io.github.nefilim.gradle.semver

import io.github.nefilim.gradle.semver.domain.GitRef
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class BranchSpec: WordSpec() {
    init {
        "Branch" should {
            "sanitize names correctly" {
                GitRef.Branch("feature/something.5-bla\$bla").sanitizedName() shouldBe "feature-something.5-bla-bla"
                GitRef.Branch("feature/something.5-bla\$bla").sanitizedNameWithoutPrefix() shouldBe "something.5-bla-bla"
                GitRef.Branch("feature/something_other.5").sanitizedNameWithoutPrefix() shouldBe "something_other.5"
            }
        }
    }
}