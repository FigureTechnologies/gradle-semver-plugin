import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION as KOTLIN_VERSION

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    signing
    alias(libs.plugins.github.release)
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.semver)
    alias(libs.plugins.versions)

    id("com.figure.publishing") // maven and gradle publishing info - build-logic/publishing
}

semver {
    tagPrefix("v")
    initialVersion("0.0.1")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
    findProperty("semver.modifier")?.toString()?.let { versionModifier(buildVersionModifier(it)) } // this is only used for non user defined strategies, ie predefined Flow or Flat
}

val invalidQualifiers = setOf("alpha", "beta", "rc", "nightly")
fun hasInvalidQualifier(candidate: ModuleComponentIdentifier): Boolean {
    return invalidQualifiers.any { candidate.version.contains(it) }
}
configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(libs.versions.kotlin.get())
            }
        }
        componentSelection {
            all {
                if (!(candidate.group.startsWith("com.figure") || (candidate.group.startsWith("io.provenance"))) && hasInvalidQualifier(candidate))
                    reject("invalid qualifier versions for $candidate")
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    api(kotlin("stdlib-jdk8"))
    implementation(libs.arrow.core)
    implementation(libs.eclipse.jgit.eclipseJgit)
    runtimeOnly(libs.eclipse.jgit.ssh.apache)
    api(libs.swiftzer.semver)
    testImplementation(gradleTestKit())
    testImplementation(libs.bundles.kotest)
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
    target {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf("-version", "-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
                jvmTarget = "11"
                languageVersion = "1.6"
                apiVersion = "1.6"
                verbose = true
            }
        }
    }
}

/*
 * Project information
 */
group = "com.figure.gradle"
description = "Figure modified Git Flow based semver plugin"
version = semver.version

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = sourceCompatibility
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}


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
