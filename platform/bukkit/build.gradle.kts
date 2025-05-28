repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")

    // more stable replacement for jitpack
    maven("https://repository.derklaro.dev/releases/") {
        mavenContent {
            releasesOnly()
        }
    }
    maven("https://repository.derklaro.dev/snapshots/") {
        mavenContent {
            snapshotsOnly()
        }
    }

}

dependencies {
    api(project(":api"))
    api(project(":platform:common"))

    implementation("com.github.retrooper:packetevents-spigot:2.8.0")
    implementation("io.papermc:paperlib:1.0.8")
    implementation("io.leangen.geantyref:geantyref:2.0.1")

    compileOnly("com.github.dmulloy2:ProtocolLib:2c0d632dc2")

    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
}
