package io.github.nefilim.gradle.semver

import arrow.core.getOrElse
import arrow.core.getOrHandle
import arrow.core.toOption
import com.javiersc.semver.Version
import io.github.nefilim.gradle.semver.config.PluginConfig
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.SemVerPluginContext
import io.github.nefilim.gradle.semver.config.Stage
import io.github.nefilim.gradle.semver.config.possiblyPrefixedVersion
import io.github.nefilim.gradle.semver.domain.GitRef
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class SemVerExtension @Inject constructor(objects: ObjectFactory, private val project: Project) {
    private val verbose: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    private val tagPrefix: Property<String> = objects.property(String::class.java).convention(PluginConfig.DefaultTagPrefix)
    private val initialVersion: Property<Version> = objects.property(Version::class.java).convention(PluginConfig.DefaultVersion)
    private val overrideVersion: Property<Version> = objects.property(Version::class.java).convention(null)

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
            initialVersion.set(Version(it))
            initialVersion.disallowChanges()
        }
    }
    fun overrideVersion(version: String) {
        overrideVersion.set(possiblyPrefixedVersion(version, tagPrefix.get())) // not great, requires tagPrefix to be set first
        overrideVersion.disallowChanges()
    }

    // defer version calculation since all our properties are lazy and needs to be configured first
    fun version(): Version {
        val config = this.buildPluginConfig()
        val context = SemVerPluginContext(project.git, config, project)
        context.log("semver configuration: $config")

        return config.overrideVersion.getOrElse {
            context.calculateVersionFlow().getOrHandle {
                context.error("failed to calculate version: $it".red())
                throw Exception("$it")
            }
        }
    }
    fun versionTagName(): String = "$tagPrefix${version()}"

    private val main: BranchHandler = BranchHandler(objects, GitRef.MainBranch.DefaultScope, GitRef.MainBranch.DefaultStage)
    private val develop: BranchHandler = BranchHandler(objects, GitRef.DevelopBranch.DefaultScope, GitRef.DevelopBranch.DefaultStage)
    private val feature: BranchHandler = BranchHandler(objects, GitRef.FeatureBranch.DefaultScope, GitRef.FeatureBranch.DefaultStage)
    private val hotfix: BranchHandler = BranchHandler(objects, GitRef.HotfixBranch.DefaultScope, GitRef.HotfixBranch.DefaultStage)

    fun main(action: Action<BranchHandler>) {
        action.execute(main)
    }
    fun develop(action: Action<BranchHandler>) {
        action.execute(develop)
    }
    fun feature(action: Action<BranchHandler>) {
        action.execute(feature)
    }
    fun hotfix(action: Action<BranchHandler>) {
        action.execute(hotfix)
    }

    private fun buildPluginConfig(): PluginConfig {
        return PluginConfig(
            verbose.get(),
            tagPrefix.get(),
            initialVersion.get(),
            overrideVersion.orNull.toOption(),
            main.scope.get(),
            main.stage.get(),
            develop.scope.get(),
            develop.stage.get(),
            feature.scope.get(),
            feature.stage.get(),
            hotfix.scope.get(),
            hotfix.stage.get(),
        )
    }

    companion object {
        const val ExtensionName = "semver"

        internal fun Project.semver(): SemVerExtension = extensions.create(ExtensionName, SemVerExtension::class.java)
    }
}

open class BranchHandler @Inject constructor(objects: ObjectFactory, private val defaultScope: Scope, private val defaultStage: Stage) {
    internal val scope: Property<Scope> = objects.property(Scope::class.java)
    internal val stage: Property<Stage> = objects.property(Stage::class.java)

    init {
        scope.set(defaultScope)
        stage.set(defaultStage)
    }

    fun scope(scope: String) {
        this.scope.set(Scope.fromValue(scope, defaultScope))
        this.scope.disallowChanges()
    }

    fun stage(stage: String) {
        this.stage.set(Stage.fromValue(stage, defaultStage))
        this.stage.disallowChanges()
    }
}