plugins {
    id("com.gradle.plugin-publish") // java-gradle-plugin and maven-publish included
    signing
}

tasks.withType<Sign>().configureEach {
    notCompatibleWithConfigurationCache("https://github.com/gradle/gradle/issues/13470")
}

/*
 * Project information
 */
group = "com.figure.gradle"
description = "Gradle Plugin for Automated Semantic Versioning"

inner class ProjectInfo {
    val name = "Gradle Semver Plugin"
    val description = "Gradle Plugin for Automated Semantic Versioning"
    val pluginImplementationClass = "$group.semver.SemverPlugin"
    val tags = listOf("semver", "git semver", "versioning")
    val website = "https://github.com/FigureTechnologies/gradle-semver-plugin"
    val vcsUrl = "https://github.com/FigureTechnologies/gradle-semver-plugin.git"
    val scmUrl = "scm:git:git://github.com/FigureTechnologies/gradle-semver-plugin.git"
}
val info = ProjectInfo()

// specific section for gradle portal
gradlePlugin {
    website.set(info.website)
    vcsUrl.set(info.vcsUrl)
    plugins {
        create(project.name) {
            id = "$group.${project.name}"
            displayName = info.name
            description = info.description
            tags.set(info.tags)
            implementationClass = info.pluginImplementationClass
        }
    }
}

afterEvaluate {
    publishing {
        publications.filterIsInstance<MavenPublication>().forEach {
            it.pom {
                name.set(info.name)
                description.set(info.description)
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("figure-oss")
                        name.set("Figure OSS Engineers")
                        email.set("oss@figure.com")
                    }
                    developer {
                        id.set("ahatzz11")
                        name.set("Alex Hatzenbuhler")
                        email.set("ahatzenbuhler@figure.com")
                    }
                    developer {
                        id.set("tcrawford")
                        name.set("Tyler Crawford")
                        email.set("tcrawford@figure.com")
                    }
                    developer {
                        id.set("jonasg13")
                        name.set("Jonas Gorauskas")
                        email.set("jgorauskas@figure.com")
                    }
                }
                scm {
                    connection.set(info.scmUrl)
                    developerConnection.set(info.scmUrl)
                    url.set(info.website)
                }
            }
        }
    }
}
