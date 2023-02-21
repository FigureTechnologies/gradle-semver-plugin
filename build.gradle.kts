import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION as KOTLIN_VERSION

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.github.release)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.semver)
    alias(libs.plugins.dependency.analysis)

    id("local.publishing") // maven and gradle publishing info - build-logic/publishing
    id("local.analysis-conventions")
     id("local.licenser")
}

semver {
    tagPrefix("v")
    initialVersion("0.0.1")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
    findProperty("semver.modifier")?.toString()
        ?.let { versionModifier(buildVersionModifier(it)) } // this is only used for non user defined strategies, ie predefined Flow or Flat
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(libs.versions.kotlin.get())
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    listOf(
        gradleApi(),
        gradleKotlinDsl(),
        libs.eclipse.jgit.eclipseJgit,
    ).forEach {
        implementation(it)
    }

    // Leak semver library users of this plugin so that they can implement their own versionModifier strategy
    api(libs.swiftzer.semver)

    listOf(
        gradleTestKit(),
        libs.bundles.kotest
    ).forEach {
        testImplementation(it)
    }
}

// Enforce Kotlin version coherence
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin")) {
            useVersion(KOTLIN_VERSION)
            because("All Kotlin modules should use the same version, and compiler uses $KOTLIN_VERSION")
        }
    }
}

kotlin {
    // Configures Java toolchain both for Kotlin JVM and Java tasks
    jvmToolchain(11)
    target {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs =
                    freeCompilerArgs + listOf("-version", "-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
                verbose = true
            }
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*TestLogEvent.values())
        exceptionFormat = TestExceptionFormat.FULL
    }
}

// project version, also used for publishing
version = semver.version

val githubTokenValue = findProperty("githubToken")?.toString() ?: System.getenv("GITHUB_TOKEN")

githubRelease {
    token(githubTokenValue)
    owner("FigureTechnologies")
    repo("gradle-semver-plugin")
    tagName(semver.versionTagName)
    targetCommitish("main")
    body("")
    generateReleaseNotes(true)
    draft(false)
    prerelease(false)
    overwrite(false)
    dryRun(false)
    apiEndpoint("https://api.github.com")
    client
}

ktlint {
    disabledRules.set(setOf("trailing-comma-on-declaration-site", "trailing-comma-on-call-site"))
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

logger.lifecycle("JDK toolchain version: ${java.toolchain.languageVersion.get()}")
logger.lifecycle("Kotlin version: ${extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension>()?.coreLibrariesVersion}")
