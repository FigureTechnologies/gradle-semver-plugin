import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION as KOTLIN_VERSION

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-gradle-plugin`
    signing
    `maven-publish`
    alias(libs.plugins.github.release)
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.semver)
    alias(libs.plugins.versions)
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

/*
 * Project information
 */
group = "com.figure.gradle"
description = "Figure modified Git Flow based semver plugin"
version = semver.version

inner class ProjectInfo {
    val longName = "Gradle Semver Plugin"
    val pluginImplementationClass = "$group.semver.SemVerPlugin"
    val tags = listOf("semver", "gitflow")
}
val info = ProjectInfo()

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

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = sourceCompatibility
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

gradlePlugin {
    plugins {
        create(project.name) {
            id = "$group.${project.name}"
            displayName = info.longName
            description = project.description
            implementationClass = info.pluginImplementationClass
        }
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
//    body()
    draft(false)
    prerelease(false)

    overwrite(false)
    dryRun(false)
    apiEndpoint("https://api.github.com")
    client
}

publishing {
    repositories {
        maven {
            url = uri("https://nexus.figure.com/repository/figure")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
    }
}
