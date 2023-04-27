import org.gradle.kotlin.dsl.repositories

plugins {
    kotlin("jvm") version "@kotlin-version@"
    id("com.figure.gradle.semver-plugin")
}

repositories {
    mavenCentral()
}

semver {
    overrideVersion("9.9.9")
}
