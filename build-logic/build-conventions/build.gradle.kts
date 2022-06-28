plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {

    // How can we get this from the catalog in this repo?
    // If we had a published catalog artifact we could import that, but this is just from the 'main' project. :hmm:
    // Related? https://github.com/gradle/gradle/issues/15383
    implementation("com.gradle.plugin-publish:com.gradle.plugin-publish.gradle.plugin:1.0.0-rc-3")
}
