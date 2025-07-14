import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import java.lang.Integer.parseInt
import java.net.URI

plugins {
    kotlin("jvm") version "2.1.21" apply false
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("org.jetbrains.dokka") version "2.0.0"
}

defaultTasks("clean", "build")

allprojects {
    group = project.property("GROUP")!!.toString()
    version = project.property("VERSION_NAME")!!.toString()

    repositories {
        mavenCentral()
    }
}


val javaVersion = project.property("JAVA_VERSION")!!.toString()

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    if (project.name != "platform") {
        apply(plugin = "com.vanniktech.maven.publish")
    }
    apply(plugin = "org.jetbrains.dokka")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion
        }
    }

    tasks.withType<JavaCompile> {
        options.release.set(parseInt(javaVersion))
    }

    tasks.withType<Jar> {
        from(rootProject.file("LICENSE"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    if (project.name != "platform") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()

            publishing {
                repositories {
                    maven {
                        name = "githubPackages"
                        url = uri("https://maven.pkg.github.com/Dani-error/velar")
                        credentials(PasswordCredentials::class)
                    }
                }
            }

            pom {
                name = "Velar"
                description = project.property("DESCRIPTION")!!.toString()
                inceptionYear = project.property("INCEPTION_YEAR")!!.toString()
                url = project.property("PROJECT_URL")!!.toString()
                licenses {
                    license {
                        name = project.property("LICENSE_NAME")!!.toString()
                        url = project.property("LICENSE_URL")!!.toString()
                    }
                }
                ciManagement {
                    system.set(project.property("CI_SYSTEM")!!.toString())
                    url.set(project.property("CI_URL")!!.toString())
                }
                developers {
                    developer {
                        id = project.property("DEVELOPER_ID")!!.toString()
                        name = project.property("DEVELOPER_NAME")!!.toString()
                        url = project.property("DEVELOPER_URL")!!.toString()
                    }
                }
                scm {
                    val repoUrl = project.property("PROJECT_URL")!!.toString()
                    url = repoUrl
                    connection = "scm:git:git://github.com/Dani-error/velar.git"
                    developerConnection = "scm:git:ssh://git@github.com/Dani-error/velar.git"
                }
            }
        }
    }
}
