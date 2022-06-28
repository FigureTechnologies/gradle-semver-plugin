import org.gradle.kotlin.dsl.`java-gradle-plugin`

plugins {
    `maven-publish`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

inner class ProjectInfo {
    val longName = "Gradle Semver Plugin"
    val description = "Gradle Plugin for Automated Semantic Versioning"
    val pluginImplementationClass = "$group.semver.SemverPlugin"
    val tags = listOf("semver", "gradle", "gitflow", "gitubflow")
    val website = "https://github.com/FigureTechnologies/gradle-semver-plugin"
    val vcsURL = "https://github.com/FigureTechnologies/gradle-semver-plugin.git"
}
val info = ProjectInfo()

/**
 * Eventually we will be able to merge the pluginBundle and gradlePlugin section.
 * Unfortunately, we can't do that gradle 7.6 is out, so we wait.
 *
 * Gradle 8+ will remove the pluginBundle
 * Link: https://plugins.gradle.org/docs/publish-plugin-new
 */

pluginBundle {
    website = info.website
    vcsUrl = info.vcsURL
    tags = info.tags

    // shared description
//    description = info.description
}

gradlePlugin {

    plugins {
        create(project.name) {
            id = "$group.${project.name}"
            displayName = info.longName
            description = info.description
            implementationClass = info.pluginImplementationClass
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://nexus.figure.com/repository/figure")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                // This line is what includes the java{} block, aka javadocs and sources
                from(components["java"])

                name.set(info.longName)
                description.set(info.description)
                url.set(info.website)
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("ahatzz11")
                        name.set("Alex Hatzenbuhler")
                        email.set("ahatzenbuhler@figure.com")
                    }
                    developer {
                        id.set("happyphan")
                        name.set("Emily Harris")
                        email.set("eharris@figure.com")
                    }
                    developer {
                        id.set("luinstra")
                        name.set("Jeremy Luinstra")
                        email.set("jluinstra@figure.com")
                    }
                    developer {
                        id.set("jonasg13")
                        name.set("Jonas Gorauskas")
                        email.set("jgorauskas@figure.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/FigureTechnologies/gradle-semver-plugin.git")
                    developerConnection.set("scm:git:ssh://github.com/FigureTechnologies/gradle-semver-plugin.git")
                    url.set(info.website)
                }
            }
        }
    }
}
