package io.github.nefilim.gradle.semver

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.getOrElse
import arrow.core.getOrHandle
import arrow.core.toOption
import io.github.nefilim.gradle.semver.config.PluginConfig
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.config.Stage
import io.github.nefilim.gradle.semver.config.possiblyPrefixedVersion
import io.github.nefilim.gradle.semver.domain.GitRef
import io.github.nefilim.gradle.semver.domain.SemVerError
import net.swiftzer.semver.SemVer
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SemVerExtension @Inject constructor(objects: ObjectFactory, private val project: Project) {
    private val verbose: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    private val tagPrefix: Property<String> = objects.property(String::class.java).convention(PluginConfig.DefaultTagPrefix)
    private val initialVersion: Property<SemVer> = objects.property(SemVer::class.java).convention(PluginConfig.DefaultVersion)
    private val overrideVersion: Property<SemVer> = objects.property(SemVer::class.java).convention(null)
    private val featureBranchRegexes: ListProperty<Regex> = objects.listProperty(Regex::class.java).convention(listOf(GitRef.FeatureBranch.DefaultRegex))

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
    fun featureBranchRegex(regex: List<String>) {
        featureBranchRegexes.add(GitRef.FeatureBranch.DefaultRegex)
        featureBranchRegexes.addAll(regex.map { it.toRegex() })
    }
    fun addFeatureBranchRegex(regex: String) {
        featureBranchRegexes.add(regex.toRegex())
    }

    // defer version calculation since all our properties are lazy and needs to be configured first
    private fun version(): SemVer {
        val config = this.buildPluginConfig()
        val git = project.git
        val context = SemVerPluginContext(config)
        val gitVersionCalculationOperations = context.getGitVersionCalculationOperations(git, git.tagMap(config.tagPrefix))
        context.verbose("semver configuration while calculating version: $config")

        return config.overrideVersion.getOrElse {
            context.calculateVersion(git, gitVersionCalculationOperations).getOrHandle {
                context.error("failed to calculate version: $it".red())
                throw Exception("$it")
            }
        }.also {
            context.log("semver: $it")
        }
    }
    private fun versionTagName(): String = tagPrefix.map { "$it${version}" }.get()

    val version by lazy { version().toString() }
    val versionTagName by lazy { versionTagName() }

    private val currentBranch: BranchHandler = BranchHandler(objects)

    fun currentBranch(action: Action<BranchHandler>) {
        action.execute(currentBranch)
    }

    private fun buildPluginConfig(): PluginConfig {
        return PluginConfig(
            verbose.get(),
            tagPrefix.get(),
            initialVersion.get(),
            overrideVersion.orNull.toOption(),
            featureBranchRegexes.get().toList(),
            currentBranch.scope.orNull.toOption(),
            currentBranch.stage.orNull.toOption(),
        )
    }

    companion object {
        const val ExtensionName = "semver"

        internal fun Project.semver(): SemVerExtension = extensions.create(ExtensionName, SemVerExtension::class.java)
    }
}

open class BranchHandler @Inject constructor(objects: ObjectFactory) {
    internal val scope: Property<Scope?> = objects.property(Scope::class.java).convention(null)
    internal val stage: Property<Stage?> = objects.property(Stage::class.java).convention(null)

    fun scope(scope: String?) {
        scope?.let {
            if (it.isNotBlank()) {
                this.scope.set(Scope.fromValue(it))
            }
        }
    }

    fun stage(stage: String?) {
        stage?.let {
            if (it.isNotBlank()) {
                this.stage.set(Stage.fromValue(it))
            }
        }
    }
}

internal fun SemVerPluginContext.calculateVersion(
    git: Git,
    ops: VersionCalculationOperations,
): Either<SemVerError, SemVer> {
    verbose("current branch: ${git.repository.fullBranch}")

    return either.eager {
        val allBranches = Either.catch { git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().toList() }.mapLeft { SemVerError.Git(it) }.bind()
        val mainRefName = allBranches.firstOrNull { GitRef.MainBranch.PossibleBranchRefs.contains(it.name) }?.name
        val developRefName = allBranches.firstOrNull { setOf(GitRef.DevelopBranch.RefName, GitRef.DevelopBranch.RemoteOriginRefName).contains(it.name) }?.name
        when {
            mainRefName == null -> {
                warnMissingRequiredBranch(GitRef.MainBranch.Name)
                config.initialVersion
            }
            developRefName == null -> {
                warn("missing [develop] branch, reverting to flat mode, all versions will be calculated from [$mainRefName]")
                val main = buildBranch(git, mainRefName, config).bind() as GitRef.MainBranch
                val current = buildCurrentBranch(git, config.flatMode()).bind()
                calculatedVersionFlat(main, current, ops).bind()
            }
            else -> {
                verbose("found main: $mainRefName, develop: $developRefName")
                val main = buildBranch(git, mainRefName, config).bind() as GitRef.MainBranch
                val develop = buildBranch(git, developRefName, config).bind() as GitRef.DevelopBranch
                val current = buildCurrentBranch(git, config).bind()

                calculatedVersionFlow(
                    main,
                    develop,
                    current,
                    ops
                ).bind()
            }
        }
    }
}

private fun SemVerPluginContext.warnMissingRequiredBranch(branchName: String) {
    warn("""
        |could not find [$branchName] branch, defaulting to initial version: ${config.initialVersion}, please create the following required branches:
        | main
        |   ∟ develop
        """.trimMargin())
}