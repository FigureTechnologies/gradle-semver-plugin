plugins {
    `kotlin-dsl`
}

dependencies {
    listOf(
        libs.gradle.plugin.publish
    ).forEach {
        implementation(it)
    }
}
