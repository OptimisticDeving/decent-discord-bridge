import com.google.gson.JsonArray
import com.google.gson.JsonObject

plugins {
    kotlin("jvm") version "2.2.21"
    id("dev.architectury.loom") version "1.13-SNAPSHOT"
}

loom {
    forge {
        mixinConfig("decent-discord-bridge.mixins.json")
    }
}

group = "dev.optimistic"
version = "1.5.2"

val jij: Configuration by configurations.creating

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://thedarkcolour.github.io/KotlinForForge")
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings(loom.layered {
        officialMojangMappings()
        //parchment("org.parchmentmc.data:parchment-1.21.8:2025.07.20@zip")
    })
    forge("net.minecraftforge:forge:1.20.1-47.4.13")
    implementation("thedarkcolour:kotlinforforge:4.12.0")

    // config
    jij("org.spongepowered:configurate-core:4.2.0")
    jij("org.spongepowered:configurate-extra-kotlin:4.2.0") {
        isTransitive = false
    }
    jij("me.lucko.configurate:configurate-toml:4.1") {
        exclude(module = "gson")
    }

    // discord
    jij("com.squareup.okhttp3:okhttp-jvm:5.1.0") {
        isTransitive = false // LexForge breaking everything as always
    }
    jij("net.dv8tion:JDA:6.1.3") {
        exclude(module = "okhttp-jvm")
        exclude(module = "slf4j-api")
        exclude(group = "org.jetbrains.kotlin")

        // Audio
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
    jij("club.minnced:jda-ktx:0.12.0") {
        isTransitive = false
    }
    jij("club.minnced:discord-webhooks:0.8.4") {
        exclude(module = "slf4j-api")
        exclude(group = "org.jetbrains.kotlin")
    }
}

configurations {
    compileClasspath {
        extendsFrom(jij)
    }

    runtimeClasspath {
        extendsFrom(jij)
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
        dependsOn(jij)

        jij.forEach { file ->
            from(file.path) {
                rename { "META-INF/jarjar/${file.name}" }
            }
        }

        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }

        filesMatching("META-INF/jarjar/metadata.json") {
            val jarsJson = JsonArray()
            jij.forEach {
                val identifier = JsonObject()
                identifier.addProperty("group", "dev.optimistic.decentdiscordbridge")
                identifier.addProperty("artifact", it.nameWithoutExtension)

                val version = JsonObject()
                version.addProperty("range", "0.0.0")
                version.addProperty("artifactVersion", "0.0.0")

                val jsonObject = JsonObject()
                jsonObject.addProperty("path", "META-INF/jarjar/${it.name}")
                jsonObject.add("identifier", identifier)
                jsonObject.add("version", version)
                jarsJson.add(jsonObject)
            }

            expand("jars" to jarsJson.toString())
        }
    }
}
