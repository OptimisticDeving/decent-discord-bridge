pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "decent-discord-bridge"