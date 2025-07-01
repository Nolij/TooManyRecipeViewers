plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://maven.taumc.org/releases")
}

dependencies {
    implementation("org.taumc.launcher:launcher-core:0.1.0-dev.4")
}