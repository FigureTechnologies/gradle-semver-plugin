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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MatchersKtTest : FunSpec({
    test("should strip off prefix and return version") {
        "v1.2.3".stripNonSemverText() shouldBe "1.2.3"
        "libraries-v1.5.4".stripNonSemverText() shouldBe "1.5.4"
    }

    test("should return input if no version found") {
        "no-version-here".stripNonSemverText() shouldBe "no-version-here"
    }

    test("should handle empty input") {
        "".stripNonSemverText() shouldBe ""
    }

    test("should handle input with only version") {
        "2.3.4".stripNonSemverText() shouldBe "2.3.4"
    }
})
