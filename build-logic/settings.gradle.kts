rootProject.name = "build-logic"

// This is for the kotlin-dsl
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()

        gradlePluginPortal() // so that external plugins can be resolved in dependencies section
    }

    // kinda hacky way to make a catalog from our own catalog, but it works!
    // Link: https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

plugins {

}

include("build-conventions")
