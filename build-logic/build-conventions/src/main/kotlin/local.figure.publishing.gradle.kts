//import org.gradle.kotlin.dsl.`java-gradle-plugin`

plugins {
    id("com.gradle.plugin-publish") // java-gradle-plugin and maven-publish included
    signing
}

/*
 * Project information
 */
group = "com.figure.gradle"
description = "Gradle Plugin for Automated Semantic Versioning"

inner class ProjectInfo {
    val longName = "Gradle Semver Plugin"
    val description = "Gradle Plugin for Automated Semantic Versioning"
    val pluginImplementationClass = "$group.semver.SemverPlugin"
    val tags = listOf("semver", "git semver", "versioning")
    val website = "https://github.com/FigureTechnologies/gradle-semver-plugin"
    val vcsUrl = "https://github.com/FigureTechnologies/gradle-semver-plugin.git"
}
val info = ProjectInfo()

// specific section for gradle portal
gradlePlugin {
    website.set(info.website)
    vcsUrl.set(info.vcsUrl)
    plugins {
        create(project.name) {
            id = "$group.${project.name}"
            displayName = info.longName
            description = info.description
            tags.set(info.tags)
            implementationClass = info.pluginImplementationClass
        }
    }
}

//publishing {
//    repositories {
//        sonatype {
//            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
//            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
//            username.set(findProject("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME"))
//            password.set(findProject("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD"))
//            stagingProfileId.set("858b6e4de4734a") // tech.figure staging profile id
//        }
//    }
//     publications {
//         create<MavenPublication>("mavenJava") {
//             pom {
//                 // This line is what includes the java{} block, aka javadocs and sources
//                 from(components["java"])
//
//                 name.set(info.longName)
//                 description.set(info.description)
//                 url.set(info.website)
//                 licenses {
//                     license {
//                         name.set("The Apache License, Version 2.0")
//                         url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                     }
//                 }
//                 developers {
//                     developer {
//                         id.set("ahatzz11")
//                         name.set("Alex Hatzenbuhler")
//                         email.set("ahatzenbuhler@figure.com")
//                     }
//                     developer {
//                         id.set("tcrawford")
//                         name.set("Tyler Crawford")
//                         email.set("tcrawford@figure.com")
//                     }
//                     developer {
//                         id.set("jonasg13")
//                         name.set("Jonas Gorauskas")
//                         email.set("jgorauskas@figure.com")
//                     }
//                 }
//                 scm {
//                     connection.set("scm:git:git://github.com/FigureTechnologies/gradle-semver-plugin.git")
//                     developerConnection.set("scm:git:ssh://github.com/FigureTechnologies/gradle-semver-plugin.git")
//                     url.set(info.website)
//                 }
//             }
//         }
//     }
//}

//if (!System.getenv("DISABLE_SIGNING").toBoolean()) {
//    configure<SigningExtension> {
//        sign(publications["maven"])
//    }
//}
