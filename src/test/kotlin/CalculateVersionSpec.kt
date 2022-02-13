import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import arrow.core.some
import arrow.core.toOption
import io.github.nefilim.gradle.semver.VersionCalculationOperations
import io.github.nefilim.gradle.semver.calculateNextVersion
import io.github.nefilim.gradle.semver.calculatedVersionFlat
import io.github.nefilim.gradle.semver.calculatedVersionFlow
import io.github.nefilim.gradle.semver.config.PluginConfig
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.config.Stage
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import net.swiftzer.semver.SemVer

// Kotest 5 is not functional until Gradle gets its act together and move to 1.6: https://github.com/kotest/kotest/issues/2785
class CalculateVersionSpec: WordSpec() {
    private val NotQualifiedStages = setOf(
        Stage.Snapshot,
    )
    private val NotLabelledStages = setOf(
        Stage.Final,
    )

    init {
        "calculateNextVersion" should {
            "calculate the next version correctly - qualified stages, except Branch" {
                val baseBranchVersion = SemVer(1, 2, 3)
                val baseBranch = GitRef.MainBranch(version = baseBranchVersion.some(), scope = Scope.Minor, stage = Stage.Final)
                val ops = getMockVersionCalculationOperations({ baseBranchVersion }, { "1" })

                Stage.values().filterNot { (NotQualifiedStages + NotLabelledStages + Stage.Branch).contains(it) }.forEach {
                    val currentBranch = GitRef.DevelopBranch(scope = Scope.Patch, stage = it)
                    calculateNextVersion(baseBranch, currentBranch, baseBranchVersion, ops)
                        .shouldBeRight()
                        .shouldBe(SemVer(1, 2, 4, "${it.toString().lowercase()}.1"))
                }
            }
            "calculate the next version correctly - not qualified stages" {
                val baseBranchVersion = SemVer(1, 2, 3)
                val baseBranch = GitRef.MainBranch(version = baseBranchVersion.some(), scope = Scope.Minor, stage = Stage.Final)
                val ops = getMockVersionCalculationOperations({ baseBranchVersion }, { "" })

                NotQualifiedStages.forEach {
                    val currentBranch = GitRef.DevelopBranch(scope = Scope.Patch, stage = it)
                    calculateNextVersion(baseBranch, currentBranch, baseBranchVersion, ops)
                        .shouldBeRight()
                        .shouldBe(SemVer(1, 2, 4, preRelease = it.toString().uppercase()))
                }
            }
            "calculate the next version correctly - not labelled stages" {
                val baseBranchVersion = SemVer(1, 2, 3)
                val baseBranch = GitRef.MainBranch(version = baseBranchVersion.some(), scope = Scope.Minor, stage = Stage.Final)
                val ops = getMockVersionCalculationOperations({ baseBranchVersion }, { "" })

                NotLabelledStages.forEach {
                    val currentBranch = GitRef.DevelopBranch(scope = Scope.Patch, stage = it)
                    calculateNextVersion(baseBranch, currentBranch, baseBranchVersion, ops).shouldBeRight() shouldBe SemVer(1, 2, 4)
                }
            }
            "calculate the next version correctly - Branch stage" {
                val baseBranchVersion = SemVer(1, 2, 3)
                val baseBranch = GitRef.MainBranch(version = baseBranchVersion.some(), scope = Scope.Minor, stage = Stage.Final)
                val ops = getMockVersionCalculationOperations({ baseBranchVersion }, { "1" })
                val featureName = "my_weird_feature"
                val currentBranch = GitRef.FeatureBranch(name = "feature/$featureName", scope = Scope.Patch, stage = Stage.Branch)
                calculateNextVersion(baseBranch, currentBranch, baseBranchVersion, ops)
                    .shouldBeRight()
                    .shouldBe(SemVer(1, 2, 4, "$featureName.1"))
            }
        }

        "calculateVersionFlow" should {
            "calculate the next version correctly - qualified stages, except Branch" {
                val mainBranchVersion = SemVer(1, 2, 3)
                val mainBranch = GitRef.MainBranch(version = mainBranchVersion.some(), scope = Scope.Minor, stage = Stage.Final)
                val developBranch = GitRef.DevelopBranch(scope = Scope.Patch, stage = Stage.Beta)
                val ops = getMockVersionCalculationOperations({ mainBranchVersion }, { "1" })

                setOf(
                    GitRef.DevelopBranch(scope = Scope.Patch, stage = Stage.Beta),
                    GitRef.FeatureBranch("feature/my_weird_feature", scope = Scope.Patch, stage = Stage.Beta),
                    GitRef.HotfixBranch("hotfix/my_weird_feature", scope = Scope.Patch, stage = Stage.RC),
                ).forEach { currentBranch ->
                    val context = SemVerPluginContext(buildPluginConfig(currentBranch))

                    context.calculatedVersionFlow(mainBranch, developBranch, currentBranch, ops)
                        .shouldBeRight() shouldBe SemVer(1, 2, 4, "${currentBranch.stage.toString().lowercase()}.1")
                }
            }
        }

        "calculateVersionFlat" should {
            "calculate the next version correctly - qualified stages, except Branch" {
                val mainBranchVersion = SemVer(1, 2, 3)
                val mainBranch = GitRef.MainBranch(version = mainBranchVersion.some(), scope = Scope.Minor, stage = Stage.Final)
                val ops = getMockVersionCalculationOperations({ mainBranchVersion }, { "1" })

                setOf(
                    GitRef.DevelopBranch(scope = Scope.Patch, stage = Stage.Beta),
                    GitRef.FeatureBranch("feature/my_weird_feature", scope = Scope.Patch, stage = Stage.Beta),
                ).forEach { currentBranch ->
                    val context = SemVerPluginContext(buildPluginConfig(currentBranch))

                    context.calculatedVersionFlat(mainBranch, currentBranch, ops)
                        .shouldBeRight() shouldBe SemVer(1, 2, 4, "${currentBranch.stage.toString().lowercase()}.1")
                }
            }
        }
    }
}

private fun getMockVersionCalculationOperations(
    baseBranchVersion: () -> SemVer,
    qualifyStage: () -> String,
): VersionCalculationOperations = object: VersionCalculationOperations {
    override fun calculateBaseBranchVersion(baseBranch: GitRef.Branch, currentBranch: GitRef.Branch): Either<SemVerError, Option<SemVer>> {
        return baseBranchVersion().some().right()
    }

    override fun qualifyStage(version: SemVer, baseBranch: GitRef.Branch, currentBranch: GitRef.Branch): Either<SemVerError, SemVer> {
        return version.copy(preRelease = "${version.preRelease}.${qualifyStage()}").right()
    }
}

private fun buildPluginConfig(currentBranch: GitRef.Branch): PluginConfig {
    return PluginConfig(
        true,
        PluginConfig.DefaultTagPrefix,
        PluginConfig.DefaultVersion,
        None,
        emptyList(),
        currentBranch.scope.toOption(),
        currentBranch.stage.toOption(),
    )
}