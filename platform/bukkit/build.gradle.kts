repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":platform:common"))

    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
}
