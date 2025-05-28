import java.net.URI
import java.net.URL

plugins {
    kotlin("jvm") version "2.1.21" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    if (project.name != "platform")
        apply(plugin = "maven-publish")

    group = "dev.dani"
    version = "1.0.0-SNAPSHOT"

    val targetJavaVersion = 21

    tasks.withType<JavaCompile>().configureEach {
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }

    // Configure publishing
    if (project.name != "platform") {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    groupId = "${project.group}.velar"
                    artifactId = project.name
                    version = project.version.toString()
                }
            }

            repositories {
                maven {
                    name = "GitHubPackages"
                    url = URI("https://maven.pkg.github.com/Dani-error/velar")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }

        // Ensure build also triggers publishToMavenLocal
        tasks.named("build") {
            finalizedBy("publishToMavenLocal")
        }
    }
}
