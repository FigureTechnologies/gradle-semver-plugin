plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {

    // How can we get this from the catalog in this repo?
    implementation("com.gradle.plugin-publish:com.gradle.plugin-publish.gradle.plugin:0.21.0")
}
