enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

// This is for the kotlin-dsl
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()

        gradlePluginPortal() // so that external plugins can be resolved in dependencies section
    }
}

plugins {

}

include("build-conventions")
