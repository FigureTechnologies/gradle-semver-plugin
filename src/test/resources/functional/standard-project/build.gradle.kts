plugins {
    id("com.figure.gradle.semver-plugin")
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

    isConfigurationForTests.set(true)
}

version = semver.version
