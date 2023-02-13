import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Calendar
import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION as KOTLIN_VERSION

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
//    signing
    alias(libs.plugins.github.release)
    alias(libs.plugins.kotlin.jvm)
    // alias(libs.plugins.semver)
    alias(libs.plugins.dependency.analysis)

    id("local.figure.publishing") // maven and gradle publishing info - build-logic/publishing
    id("local.analysis-conventions")

    // https://github.com/CadixDev/licenser
    id("org.cadixdev.licenser") version "0.6.1"
}

// semver {
//     tagPrefix("v")
//     initialVersion("0.0.1")
//     findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
//     findProperty("semver.modifier")?.toString()
//         ?.let { versionModifier(buildVersionModifier(it)) } // this is only used for non user defined strategies, ie predefined Flow or Flat
// }

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
                // languageVersion = "1.8"
                // apiVersion = "1.8"
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
version = "1.1.0"
// version = semver.version

val githubTokenValue = findProperty("githubToken")?.toString() ?: System.getenv("GITHUB_TOKEN")

githubRelease {
    token(githubTokenValue)
    owner("FigureTechnologies")
    repo("gradle-semver-plugin")
    // tagName(semver.versionTagName)
    tagName("v1.1.0")
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

license {
    header(project.file("HEADER.txt"))

    // use /** for kotlin files
    style.put("kt", "JAVADOC")

    // This is kinda weird in kotlin but the plugin is groovy so it works
    properties {
        this.set("year", Calendar.getInstance().get(Calendar.YEAR))
        this.set("company", "Figure Technologies")
    }

    include("**/*.kt") // Apply license header ONLY to kotlin files
}

// Ensure licenses are updated when the app is assembled
// This needs to happen early in the gradle lifecycle or else the checkLicenses task fails
tasks.named("assemble") {
    dependsOn("updateLicenses")
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

logger.lifecycle("JDK toolchain version: ${java.toolchain.languageVersion.get()}")
logger.lifecycle("Kotlin version: ${extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension>()?.coreLibrariesVersion}")
