package io.github.nefilim.gradle.semver

import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.WordSpec
import net.swiftzer.semver.SemVer

// Kotest 5 is not functional until Gradle gets its act together and move to 1.6: https://github.com/kotest/kotest/issues/2785
class SemVerSpec: WordSpec() {
    init {
        "SemVer" should {
            "match valid existing semver tags on refs" {
                "refs/tags/v123".semverTag("v").shouldBeNone()
                "refs/tags/v1.2.3".semverTag("v").shouldBeSome(SemVer(1, 2, 3))
                "refs/tags/v0.1.2-main".semverTag("v").shouldBeSome(SemVer(0, 1, 2, "main"))

                // SemVer lib can't understand qualified labels apparently
//                "refs/tags/v0.1.2-main.2".semverTag("v").shouldBeSome(SemVer(0, 1, 2, "main.2"))
//                SemVer.parse("v0.1.2-main.2") shouldBe SemVer(0, 1, 2, "main.2")
            }
        }
    }
}