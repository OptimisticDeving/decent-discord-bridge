plugins {
    kotlin("jvm") version "2.0.0"
    id("fabric-loom") version "1.7-SNAPSHOT"
}

loom {
    serverOnlyMinecraftJar()
}

group = "dev.optimistic"
version = "1.0.0"

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.11")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.11.0+kotlin.2.0.0")

    // really wishing loom properly supported gradle kts right about now
    for (coordinates in arrayOf(
        // config
        "org.spongepowered:configurate-core:4.1.2",
        "org.spongepowered:configurate-extra-kotlin:4.1.2",
        "me.lucko.configurate:configurate-toml:4.1",
        // discord
        "net.dv8tion:JDA:5.0.0-beta.24",
        "club.minnced:jda-ktx:0.11.0-beta.20",
        "club.minnced:discord-webhooks:0.8.4"
    )) {
        val split = coordinates.split(':')
        if (split.size != 3)
            throw IllegalArgumentException("invalid dependency coordinates, expected 3 groups but got ${split.size}")

        include(implementation(group = split[0], name = split[1], version = split[2]))
    }
}

java {
    withSourcesJar()

    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks {
    jar {
        from("LICENSE") { rename { "${project.name}_${it}" } }
    }

    withType<JavaCompile>().configureEach {
        options.release = 21
        options.encoding = "UTF-8"
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}