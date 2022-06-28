plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    // note: this `libs` is from build-logic/settings.gradle.kts, technically different from the 'main' project
    implementation(libs.gradle.plugin.publish)
}
