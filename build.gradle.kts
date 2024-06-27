plugins {
    kotlin("jvm") version "2.0.0"
    id("fabric-loom") version "1.7-SNAPSHOT"
}

loom {
    serverOnlyMinecraftJar()
}

group = "dev.optimistic"
version = "1.0.0"

val shaded: Configuration by configurations.creating

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.11")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.11.0+kotlin.2.0.0")

    // config
    shaded("org.spongepowered:configurate-core:4.1.2")
    shaded("org.spongepowered:configurate-extra-kotlin:4.1.2")
    shaded("me.lucko.configurate:configurate-toml:4.1")

    // discord
    shaded("net.dv8tion:JDA:5.0.0-beta.24")
    shaded("club.minnced:jda-ktx:0.11.0-beta.20")
    shaded("club.minnced:discord-webhooks:0.8.4")
}

configurations {
    compileClasspath {
        extendsFrom(shaded)
    }
}

java {
    withSourcesJar()

    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

tasks {
    jar {
        from("LICENSE") { rename { "${project.name}_${it}" } }
    }

    remapJar {
        addNestedDependencies = true
    }

    withType<JavaCompile>().configureEach {
        options.release = 17
        options.encoding = "UTF-8"
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }

        dependsOn(shaded)

        shaded.forEach {
            from(zipTree(it)) {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
            }
        }
    }
}
