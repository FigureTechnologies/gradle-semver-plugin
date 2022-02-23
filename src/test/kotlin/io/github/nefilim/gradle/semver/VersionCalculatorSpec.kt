package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.Option
import arrow.core.right
import arrow.core.some
import arrow.core.toOption
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import net.swiftzer.semver.SemVer

// Kotest 5 is not functional until Gradle gets its act together and move to 1.6: https://github.com/kotest/kotest/issues/2785
class CalculateVersionSpec: WordSpec() {
    private fun calculateBranchVersion(
        currentBranch: GitRef.Branch,
        branchVersions: Map<GitRef.Branch, SemVer>,
        config: VersionCalculatorConfig,
        commitsSinceBranchPoint: Int = 2,
    ): Either<SemVerError, SemVer> {
        val ops = getMockContextProviderOperations(currentBranch, branchVersions, commitsSinceBranchPoint)
        val context = mockSemVerContext(ops)
        val calculator = getTargetBranchVersionCalculator(ops, config, context, currentBranch)
        return calculator.calculateVersion()
    }

    init {
        "Calculate version with FlatDefaultBranchMatching" should {
            "calculate the next version correctly" {
                val mainBranchVersion = SemVer(1, 2, 3)
                val mainBranch = GitRef.Branch.Main
                val developBranchVersion = SemVer(1, 2, 4, "beta")
                val developBranch = GitRef.Branch.Develop
                val config = buildPluginConfig(FlatVersionCalculatorStrategy { nextPatch() })
                val branchVersions: Map<GitRef.Branch, SemVer> = mapOf(mainBranch to mainBranchVersion, developBranch to developBranchVersion)

                // current == main
                calculateBranchVersion(mainBranch, branchVersions, config).shouldBeRight() shouldBe mainBranchVersion.nextPatch()

                // current != main
                setOf(
                    GitRef.Branch.Develop,
                    GitRef.Branch("feature/my_weird_feature"),
                    GitRef.Branch("something"),
                    GitRef.Branch("hotfix/fix-1"),
                ).forEach { currentBranch ->
                    println("testing $currentBranch")
                    calculateBranchVersion(currentBranch, branchVersions, config).shouldBeRight() shouldBe mainBranchVersion.nextPatch().copy(preRelease = "${currentBranch.sanitizedNameWithoutPrefix()}.2")
                }
            }
        }

        "Calculate version with FlowDefaultBranchMatching" should {
            "calculate the next version correctly" {
                val mainBranchVersion = SemVer(1, 2, 3)
                val mainBranch = GitRef.Branch.Main
                val developBranchVersion = SemVer(1, 2, 4, "beta")
                val developBranch = GitRef.Branch.Develop
                val config = buildPluginConfig(FlowVersionCalculatorStrategy { nextPatch() })
                val branchVersions: Map<GitRef.Branch, SemVer> = mapOf(mainBranch to mainBranchVersion, developBranch to developBranchVersion)

                // current == main
                calculateBranchVersion(mainBranch, branchVersions, config).shouldBeRight() shouldBe mainBranchVersion.nextPatch()

                calculateBranchVersion(developBranch, branchVersions, config).shouldBeRight() shouldBe mainBranchVersion.nextPatch().copy(preRelease = "beta.2")
                calculateBranchVersion(GitRef.Branch("hotfix/something"), branchVersions, config).shouldBeRight() shouldBe mainBranchVersion.nextPatch().copy(preRelease = "rc.2")

                calculateBranchVersion(GitRef.Branch("feature/something_something*bla"), branchVersions, config)
                    .shouldBeRight()
                    .shouldBe(mainBranchVersion.nextPatch().nextPatch().copy(preRelease = "something_something-bla.2"))

            }
        }
    }
}

private fun getMockContextProviderOperations(
    currentBranch: GitRef.Branch,
    branchVersion: Map<GitRef.Branch, SemVer>,
    commitsSinceBranchPoint: Int = 2,
): ContextProviderOperations = object: ContextProviderOperations {
    override fun currentBranch(): Option<GitRef.Branch> {
        return currentBranch.some()
    }

    override fun branchVersion(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Either<SemVerError, Option<SemVer>> {
        return branchVersion
            .filter { it.key.name == targetBranch.name }.toList().firstOrNull()?.second.toOption().right()
    }

    override fun commitsSinceBranchPoint(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Either<SemVerError, Int> {
        return commitsSinceBranchPoint.right()
    }
}

private fun buildPluginConfig(
    branchMatching: List<BranchMatchingConfiguration>,
): VersionCalculatorConfig {
    return VersionCalculatorConfig(
        "v",
        initialVersion = SemVer(0, 0, 1),
        branchMatching = branchMatching,
    )
}

private fun mockSemVerContext(
    ops: ContextProviderOperations,
    props: Map<String, Any?> = emptyMap(),
    env: Map<String, String?> = emptyMap(),
): SemVerContext {
    return object: SemVerContext {
        override val ops: ContextProviderOperations
            get() = ops
        
        override fun property(name: String): Any? {
            return props[name]
        }

        override fun env(name: String): String? {
            return env[name]
        }
    }
}

