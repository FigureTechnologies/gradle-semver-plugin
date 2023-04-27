import org.gradle.api.internal.FeaturePreviews.Feature.STABLE_CONFIGURATION_CACHE
import org.gradle.api.internal.FeaturePreviews.Feature.TYPESAFE_PROJECT_ACCESSORS

rootProject.name = "semver-plugin"

enableFeaturePreview(STABLE_CONFIGURATION_CACHE.name)
enableFeaturePreview(TYPESAFE_PROJECT_ACCESSORS.name)

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }

    includeBuild("build-logic")
}

plugins {
    `gradle-enterprise`
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlwaysIf(System.getenv("GITHUB_ACTIONS") == "true")
        publishOnFailure()
    }
}
