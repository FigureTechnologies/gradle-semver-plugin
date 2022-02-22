package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.left
import io.github.nefilim.gradle.semver.config.VersionCalculatorConfig
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import net.swiftzer.semver.SemVer
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

private val logger = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

// move to value classes once Gradle moves to 1.6
typealias PreReleaseLabel = String
typealias BuildMetadataLabel = String

interface VersionCalculator {
    fun calculateVersion(): Either<SemVerError, SemVer>
}

interface ContextProviderOperations {
    fun currentBranch(): Option<GitRef.Branch>
    fun branchVersion(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Either<SemVerError, Option<SemVer>>
    fun commitsSinceBranchPoint(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch): Either<SemVerError, Int>
}

interface SemVerContext {
    fun property(name: String): Any?
    fun env(name: String): String?
    val ops: ContextProviderOperations
}

typealias VersionModifier = SemVer.() -> SemVer
typealias VersionQualifier = SemVerContext.(current: GitRef.Branch) -> Pair<PreReleaseLabel, BuildMetadataLabel>

fun getTargetBranchVersionCalculator(
    contextProviderOperations: ContextProviderOperations,
    config: VersionCalculatorConfig,
    context: SemVerContext,
    currentBranch: GitRef.Branch,
): VersionCalculator = object: VersionCalculator {

    private fun previousVersion(): Either<SemVerError, SemVer> {
        return config.branchMatching.firstOrNull {
            it.regex.matches(currentBranch.name)
        }?.let { bmc ->
            contextProviderOperations.branchVersion(currentBranch, bmc.targetBranch).map {
                logger.debug("branch version for current $currentBranch and target ${bmc.targetBranch}: $it")
                it.getOrElse {
                    logger.warn("no version found for target branch ${bmc.targetBranch}, using initial version")
                    config.initialVersion
                }
            }
        } ?: run {
            logger.warn("no match found for $currentBranch in ${config.branchMatching}, using initial version as previous version")
            SemVerError.MissingBranchMatchingConfiguration(currentBranch).left()
        }
    }

    private fun versionModifier(current: SemVer): SemVer {
        return config.branchMatching.firstOrNull {
            it.regex.matches(currentBranch.name)
        }?.let {
            val fn = it.versionModifier
            current.fn()
        } ?: run {
            logger.warn("no match found for $currentBranch in ${config.branchMatching}, using initial version as modified version")
            config.initialVersion
        }
    }

    private fun versionQualifier(current: SemVer): SemVer {
        return config.branchMatching.firstOrNull {
            it.regex.matches(currentBranch.name)
        }?.let {
            logger.info("found branch match [$it] for current [$currentBranch]")
            val fn = it.versionQualifier
            context.fn(currentBranch).let {
                current.copy(
                    preRelease = it.first.ifBlank { null },
                    buildMetadata = it.second.ifBlank { null }
                )
            }
        } ?: run {
            logger.warn("no match found for $currentBranch in ${config.branchMatching}")
            current
        }
    }

    override fun calculateVersion(): Either<SemVerError, SemVer> {
        return previousVersion().map {
            versionQualifier(versionModifier(it))
        }
    }
}

data class BranchMatchingConfiguration(
    val regex: Regex,
    val targetBranch: GitRef.Branch,
    val versionQualifier: VersionQualifier,
    val versionModifier: VersionModifier = { nextPatch() },
)

fun SemVerContext.preReleaseWithCommitCount(currentBranch: GitRef.Branch, targetBranch: GitRef.Branch, label: String): String {
    return ops.commitsSinceBranchPoint(currentBranch, targetBranch).fold({
        logger.warn("Unable to calculate commits since branch point on current $currentBranch")
        label
    }, {
        "$label.$it"
    })
}

fun FlowDefaultBranchMatching(versionModifier: VersionModifier) = listOf(
    BranchMatchingConfiguration("""^main$""".toRegex(), GitRef.Branch.Main, { "" to "" }, versionModifier),
    BranchMatchingConfiguration("""^develop$""".toRegex(), GitRef.Branch.Main, { preReleaseWithCommitCount(it, GitRef.Branch.Main, "beta") to "" }, versionModifier),
    BranchMatchingConfiguration("""^feature/.*""".toRegex(), GitRef.Branch.Develop, { current -> preReleaseWithCommitCount(current, GitRef.Branch.Main, current.sanitizedNameWithoutPrefix()) to "" }, versionModifier),
    BranchMatchingConfiguration("""^hotfix/.*""".toRegex(), GitRef.Branch.Main, { preReleaseWithCommitCount(it, GitRef.Branch.Main, "rc") to "" }, versionModifier),
)

fun FlatDefaultBranchMatching(versionModifier: VersionModifier) = listOf(
    BranchMatchingConfiguration("""^main$""".toRegex(), GitRef.Branch.Main, { "" to "" }, versionModifier),
    BranchMatchingConfiguration(""".*""".toRegex(), GitRef.Branch.Main, { preReleaseWithCommitCount(it, GitRef.Branch.Main, it.sanitizedNameWithoutPrefix()) to "" }, versionModifier),
)