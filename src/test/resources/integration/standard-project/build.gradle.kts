plugins {
    kotlin("jvm") version "@kotlin-version@"
    id("com.figure.gradle.semver-plugin")
}

repositories {
    mavenCentral()
}

semver {
    tagPrefix("v")
    initialVersion("0.0.1")

    findProperty("semver.overrideVersion")?.toString()
        ?.let { overrideVersion(it) }

    val semVerModifier = findProperty("semver.modifier")?.toString()
        ?.let { buildVersionModifier(it) }
        ?: { nextPatch() }

    versionModifier(semVerModifier)
}

version = semver.version
