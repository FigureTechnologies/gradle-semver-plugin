plugins {
    id("com.figure.gradle.semver-plugin")
}

semver {
    overrideVersion("9.9.9")
}

version = semver.version
