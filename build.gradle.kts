import com.google.gson.JsonArray
import com.google.gson.JsonObject

plugins {
    kotlin("jvm") version "2.1.20"
    id("net.minecraftforge.gradle") version "6.0.35"
    id("org.spongepowered.mixin") version "0.7.+"
}

jarJar.disable() // We have our own system
minecraft {
    mappings("official", "1.20.1")
}

group = "dev.optimistic"
version = "1.4.3"

val jij: Configuration by configurations.creating

repositories {
    maven("https://thedarkcolour.github.io/KotlinForForge/") {
        content { includeGroup("thedarkcolour") }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:1.20.1-47.4.0")
    implementation("thedarkcolour:kotlinforforge:4.11.0")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // config
    jij("org.spongepowered:configurate-core:4.2.0")
    jij("org.spongepowered:configurate-extra-kotlin:4.2.0") {
        isTransitive = false
    }
    jij("me.lucko.configurate:configurate-toml:4.1") {
        exclude(module = "gson")
    }

    // discord
    jij("net.dv8tion:JDA:5.3.1") {
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

mixin {
    add(sourceSets.main.get(), "decent-discord-bridge.refmap.json")
    config("decent-discord-bridge.mixins.json")
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

    withType<JavaCompile>().configureEach {
        options.release = 17
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
