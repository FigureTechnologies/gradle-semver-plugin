rootProject.name = "semver-plugin"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://nexus.figure.com/repository/gradle-plugins")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
        maven { // needed for p8e-gradle-plugin
            url = uri("https://nexus.figure.com/repository/mirror")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
        maven { // needed for p8e-gradle-plugin
            url = uri("https://nexus.figure.com/repository/figure")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://nexus.figure.com/repository/gradle-plugins")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
        maven { // needed for p8e-gradle-plugin
            url = uri("https://nexus.figure.com/repository/mirror")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
        maven { // needed for p8e-gradle-plugin
            url = uri("https://nexus.figure.com/repository/figure")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
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
    }
}
