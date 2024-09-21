plugins {
    id("com.gradle.plugin-publish")
    if ("CI" in System.getenv()) {
        signing
    }
}

class PublishingConstants {
    val name = "Gradle Semver Plugin"
    val description = "Gradle Plugin for Automatic Semantic Versioning"
    val pluginImplementation = "com.figure.gradle.semver.SemverPlugin"
    val tags = listOf("semver", "versioning", "git")

    val website = "https://figuretechnologies.github.io/gradle-semver-plugin"
    val vcsUrl = "https://github.com/FigureTechnologies/gradle-semver-plugin.git"
    val scmUrl = "scm:git:git://github.com/FigureTechnologies/gradle-semver-plugin.git"
}

val info = PublishingConstants()

configure<GradlePluginDevelopmentExtension> {
    website = info.website
    vcsUrl = info.vcsUrl
    plugins {
        create("semver-plugin") {
            id = "$group.semver-plugin"
            displayName = info.name
            description = info.description
            implementationClass = info.pluginImplementation
            tags = info.tags
        }
    }
}

afterEvaluate {
    configure<PublishingExtension> {
        publications {
            withType<MavenPublication> {
                pom {
                    name = info.name
                    description = info.description
                    licenses {
                        license {
                            name = "The Apache Software License, Version 2.0"
                            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                            distribution = "repo"
                        }
                    }
                    developers {
                        developer {
                            id = "figure-oss"
                            name = "Figure OSS Engineers"
                            email = "oss@figure.com"
                        }
                        developer {
                            id = "tcrawford-figure"
                            name = "Tyler Crawford"
                            email = "tcrawford@figure.com"
                        }
                        developer {
                            id = "ahatzz11"
                            name = "Alex Hatzenbuhler"
                            email = "ahatzenbuhler@figure.com"
                        }
                        developer {
                            id = "jonasg13"
                            name = "Jonas Gorauskas"
                            email = "jgorauskas@figure.com"
                        }
                    }
                    scm {
                        connection = info.scmUrl
                        developerConnection = info.scmUrl
                        url = info.website
                    }
                }
            }
        }
    }
}

