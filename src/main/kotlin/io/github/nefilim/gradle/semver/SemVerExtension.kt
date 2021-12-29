package io.github.nefilim.gradle.semver

import arrow.core.toOption
import com.javiersc.semver.Version
import io.github.nefilim.gradle.semver.config.PluginConfig
import io.github.nefilim.gradle.semver.config.Scope
import io.github.nefilim.gradle.semver.config.Stage
import io.github.nefilim.gradle.semver.domain.GitRef
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class SemVerExtension @Inject constructor(objects: ObjectFactory) {
    private val tagPrefix: Property<String> = objects.property(String::class.java)
    private val initialVersion: Property<Version> = objects.property(Version::class.java)
    private val overrideVersion: Property<Version> = objects.property(Version::class.java)

    private val main: BranchHandler = objects.newInstance(BranchHandler::class.java, objects, GitRef.MainBranch.DefaultScope, GitRef.MainBranch.DefaultStage)
    private val develop: BranchHandler = objects.newInstance(BranchHandler::class.java, objects, GitRef.DevelopBranch.DefaultScope, GitRef.DevelopBranch.DefaultStage)
    private val feature: BranchHandler = objects.newInstance(BranchHandler::class.java, objects, GitRef.FeatureBranch.DefaultScope, GitRef.FeatureBranch.DefaultStage)
    private val hotfix: BranchHandler = objects.newInstance(BranchHandler::class.java, objects, GitRef.HotfixBranch.DefaultScope, GitRef.HotfixBranch.DefaultStage)

    init {
        tagPrefix.set(PluginConfig.DefaultTagPrefix)
        initialVersion.set(PluginConfig.DefaultVersion)
        overrideVersion.set(null)
    }

    fun tagPrefix(prefix: String) {
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
        overrideVersion.set(Version(version))
        overrideVersion.disallowChanges()
    }

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

    fun buildPluginConfig(): PluginConfig {
        return PluginConfig(
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
        internal fun Project.semver(): SemVerExtension = extensions.create("semver", SemVerExtension::class.java)
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