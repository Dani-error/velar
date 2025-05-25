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

    group = "dev.dani"
    version = "1.0.0-SNAPSHOT"

    val targetJavaVersion = 21

    tasks.withType<JavaCompile>().configureEach {
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }

}
