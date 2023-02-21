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
        libs.licenser,
    ).forEach {
        implementation(it)
    }
}
