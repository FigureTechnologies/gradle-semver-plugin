import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION as KOTLIN_VERSION

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.semver)
    alias(libs.plugins.githubrelease)
}

semver {
    verbose(true)
    tagPrefix("v")
    initialVersion("0.0.1")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }

    main {
        scope(findProperty("semver.main.scope")?.toString() ?: "patch")
        stage(findProperty("semver.main.stage")?.toString() ?: "final")
    }
}

/*
 * Project information
 */
group = "io.github.nefilim.gradle"
description = "Modified Git Flow based semver plugin"
version = semver.version()

inner class ProjectInfo {
    val longName = "Gradle Semver Plugin"
    val pluginImplementationClass = "$group.semver.SemVerPlugin"
    val tags = listOf("semver", "gitflow")
    val website = "https://github.com/nefilim/gradle-semver-plugin"
}
val info = ProjectInfo()

repositories {
    mavenCentral()
}

tasks.named("publish") {
    dependsOn("signMavenJavaPublication")
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    api(kotlin("stdlib-jdk8"))
    implementation(libs.arrow.core)
    implementation(libs.eclipse.jgit.eclipseJgit)
    api(libs.javiersc.semver.semverCore)
    testImplementation(gradleTestKit())
    testImplementation(libs.bundles.kotlin.testing)
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

pluginBundle {
    website = info.website
    vcsUrl = info.website
    tags = info.tags
}

githubRelease {
    val gitHubTokenProp: String? by project

    token(gitHubTokenProp) // This is your personal access token with Repo permissions
    // You get this from your user settings > developer settings > Personal Access Tokens
    owner("nefilim") // default is the last part of your group. Eg group: "com.github.breadmoirai" => owner: "breadmoirai"
    repo("gradle-semver-plugin") // by default this is set to your project name
    tagName("v${project.version}") // by default this is set to "v${project.version}"
    targetCommitish("main") // by default this is set to "master"
    body(changelog()) // by default this is empty
    draft(false) // by default this is false
    prerelease(false) // by default this is false

    overwrite(false) // by default false; if set to true, will delete an existing release with the same tag and name
    dryRun(false) // by default false; you can use this to see what actions would be taken without making a release
    apiEndpoint("https://api.github.com") // should only change for github enterprise users
    client // This is the okhttp client used for http requests
}