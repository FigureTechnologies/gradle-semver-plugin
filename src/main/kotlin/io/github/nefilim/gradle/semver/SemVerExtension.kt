package io.github.nefilim.gradle.semver

import arrow.core.getOrElse
import arrow.core.getOrHandle
import arrow.core.toOption
import io.github.nefilim.gradle.semver.config.PluginConfig
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.config.Stage
import io.github.nefilim.gradle.semver.config.possiblyPrefixedVersion
import io.github.nefilim.gradle.semver.domain.GitRef
import net.swiftzer.semver.SemVer
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class SemVerExtension @Inject constructor(objects: ObjectFactory, private val project: Project) {
    private val verbose: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    private val tagPrefix: Property<String> = objects.property(String::class.java).convention(PluginConfig.DefaultTagPrefix)
    private val initialVersion: Property<SemVer> = objects.property(SemVer::class.java).convention(PluginConfig.DefaultVersion)
    private val overrideVersion: Property<SemVer> = objects.property(SemVer::class.java).convention(null)
    private val featureBranchRegexes: ListProperty<Regex> = objects.listProperty(Regex::class.java).convention(listOf(GitRef.FeatureBranch.DefaultRegex))

    fun verbose(b: Boolean) {
        verbose.set(b)
        verbose.disallowChanges()
    }
    fun tagPrefix(prefix: String) {
        if (overrideVersion.orNull != null)
            throw IllegalArgumentException("cannot set the semver tagPrefix after override version has been set, the override version depends on the tagPrefix, set the tagPrefix first")
        tagPrefix.set(prefix)
        tagPrefix.disallowChanges()
    }
    fun initialVersion(version: String?) {
        version?.also {
            initialVersion.set(SemVer.parse(it))
            initialVersion.disallowChanges()
        }
    }
    fun overrideVersion(version: String) {
        overrideVersion.set(possiblyPrefixedVersion(version, tagPrefix.get())) // not great, requires tagPrefix to be set first
        overrideVersion.disallowChanges()
    }
    fun featureBranchRegex(regex: List<String>) {
        featureBranchRegexes.add(GitRef.FeatureBranch.DefaultRegex)
        featureBranchRegexes.addAll(regex.map { it.toRegex() })
        featureBranchRegexes.disallowChanges()
    }

    // defer version calculation since all our properties are lazy and needs to be configured first
    private fun version(): SemVer {
        val config = this.buildPluginConfig()
        val context = SemVerPluginContext(project.git, config, project)
        context.verbose("semver configuration while calculating version: $config")

        return config.overrideVersion.getOrElse {
            context.calculateVersion().getOrHandle {
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

    internal fun buildPluginConfig(): PluginConfig {
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
                this.scope.disallowChanges()
            }
        }
    }

    fun stage(stage: String?) {
        stage?.let {
            if (it.isNotBlank()) {
                this.stage.set(Stage.fromValue(it))
                this.stage.disallowChanges()
            }
        }
    }
}