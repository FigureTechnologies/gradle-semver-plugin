package io.github.nefilim.gradle.semver

import arrow.core.getOrElse
import arrow.core.getOrHandle
import arrow.core.toOption
import io.github.nefilim.gradle.semver.config.VersionCalculatorConfig
import io.github.nefilim.gradle.semver.domain.GitRef
import net.swiftzer.semver.SemVer
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

private val logger = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

abstract class SemVerExtension @Inject constructor(objects: ObjectFactory, private val project: Project) {
    private val verbose: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    private val tagPrefix: Property<String> = objects.property(String::class.java).convention(VersionCalculatorConfig.DefaultTagPrefix)
    private val initialVersion: Property<SemVer> = objects.property(SemVer::class.java).convention(VersionCalculatorConfig.DefaultVersion)
    private val overrideVersion: Property<SemVer> = objects.property(SemVer::class.java).convention(null)
    private val branchMatching: ListProperty<BranchMatchingConfiguration> = objects.listProperty(BranchMatchingConfiguration::class.java).convention(FlowDefaultBranchMatching { nextPatch() })

    // TODO UGHHHHH
    private var versionModifier: VersionModifier = { nextPatch() }

    fun verbose(b: Boolean) {
        verbose.set(b)
    }
    fun tagPrefix(prefix: String) {
        if (overrideVersion.orNull != null)
            throw IllegalArgumentException("cannot set the semver tagPrefix after override version has been set, the override version depends on the tagPrefix, set the tagPrefix first")
        tagPrefix.set(prefix)
    }
    fun initialVersion(version: String?) {
        version?.also {
            initialVersion.set(SemVer.parse(it))
        }
    }
    fun overrideVersion(version: String) {
        overrideVersion.set(possiblyPrefixedVersion(version, tagPrefix.get())) // not great, requires tagPrefix to be set first
    }
    fun versionModifier(modifier: VersionModifier) {
        this.versionModifier = modifier
    }
    fun versionModifier(modifier: String) {
        when (val mod = modifier.trim().lowercase()) {
            "major" -> versionModifier { nextMajor() }
            "minor" -> versionModifier { nextMinor() }
            "patch" -> versionModifier { nextPatch() }
            else -> {
                logger.error("unknown version modifier [$mod]")
                throw Exception("unknown version modifier [$mod]")
            }
        }
    }

    // defer version calculation since all our properties are lazy and needs to be configured first
    private fun version(): SemVer {
        val git = project.git
        val config = if (git.hasBranch(GitRef.Branch.Develop.name).isNotEmpty()) {// if we have a develop branch, assume Git Flow hybrid
            logger.semver("enabling Git Flow mode")
            buildCalculatorConfig().withBranchMatchingConfig(FlowDefaultBranchMatching(versionModifier))
        } else { // if we don't have a develop branch, fallback to Flat mode
            logger.semver("enabling Flat mode")
            buildCalculatorConfig().withBranchMatchingConfig(FlatDefaultBranchMatching(versionModifier))
        }
        val ops = getGitContextProviderOperations(git, config)
        val context = GradleSemVerContext(project, ops)

        return ops.currentBranch().fold({
            logger.error("failed to find current branch, cannot calculate semver".red())
            throw Exception("failed to find current branch")
        }, { currentBranch ->
            val calculator = getTargetBranchVersionCalculator(ops, config, context, currentBranch)
            logger.info("semver configuration while calculating version: $config")

            config.overrideVersion.getOrElse {
                calculator.calculateVersion().getOrHandle {
                    logger.error("failed to calculate version: $it".red())
                    throw Exception("$it")
                }
            }.also {
                logger.info("semver: $it".bold())
            }
        })
    }
    private fun versionTagName(): String = tagPrefix.map { "$it${version}" }.get()
    private fun possiblyPrefixedVersion(version: String, prefix: String): SemVer {
        return SemVer.parse(version.trimMargin(prefix)) // fail fast, don't let an invalid version propagate to runtime
    }

    val version by lazy { version().toString() }
    val versionTagName by lazy { versionTagName() }

    private fun buildCalculatorConfig(): VersionCalculatorConfig {
        return VersionCalculatorConfig(
            tagPrefix.get(),
            initialVersion.get(),
            overrideVersion.orNull.toOption(),
        )
    }

    companion object {
        const val ExtensionName = "semver"

        internal fun Project.semver(): SemVerExtension = extensions.create(ExtensionName, SemVerExtension::class.java)
    }
}