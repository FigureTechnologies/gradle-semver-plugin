plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    listOf(
        libs.gradle.plugin.publish,
        libs.ktlint,
        libs.detekt,
    ).forEach {
        implementation(it)
    }
}
