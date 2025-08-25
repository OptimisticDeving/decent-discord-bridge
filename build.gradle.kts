import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.BufferedOutputStream
import java.nio.file.Files
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

plugins {
    kotlin("jvm") version "2.2.10"
    id("fabric-loom") version "1.11-SNAPSHOT"
}

loom {
    serverOnlyMinecraftJar()
}

group = "dev.optimistic"
version = "1.5.1"

val jij: Configuration by configurations.creating

repositories {
    maven("https://maven.parchmentmc.org")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.8")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.8:2025.07.20@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:0.17.2")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.5+kotlin.2.2.10")

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
            val inputStream = JarInputStream(file.inputStream())
            val path = Files.createTempFile("giggle", "gaggle")
            val fileOutputStream = BufferedOutputStream(Files.newOutputStream(path))
            val jarOutputStream = JarOutputStream(fileOutputStream)
            var entry = inputStream.nextJarEntry

            while (entry != null) {
                jarOutputStream.putNextEntry(entry as ZipEntry)
                val entryBytes = inputStream.readAllBytes()
                jarOutputStream.write(entryBytes, 0, entryBytes.size)
                jarOutputStream.closeEntry()
                inputStream.closeEntry()
                entry = inputStream.nextJarEntry
            }

            jarOutputStream.putNextEntry(ZipEntry("fabric.mod.json"))

            val fabricModJson = JsonObject()
            fabricModJson.addProperty("schemaVersion", 1)
            fabricModJson.addProperty("id", file.nameWithoutExtension.lowercase().replace(Regex("[^a-z0-9_-]"), "_"))
            fabricModJson.addProperty("version", "0.0.0")
            fabricModJson.addProperty("name", file.nameWithoutExtension)

            val jsonBytes = Gson().toJson(fabricModJson).encodeToByteArray()
            jarOutputStream.write(jsonBytes, 0, jsonBytes.size)
            jarOutputStream.closeEntry()
            jarOutputStream.flush()
            jarOutputStream.close()
            fileOutputStream.flush()
            fileOutputStream.close()

            from(path) {
                rename { "META-INF/jars/${file.name}" }
            }
        }

        filesMatching("fabric.mod.json") {
            val jarsJson = JsonArray()
            jij.forEach {
                val jsonObject = JsonObject()
                jsonObject.addProperty("file", "META-INF/jars/${it.name}")
                jarsJson.add(jsonObject)
            }

            expand(
                "version" to project.version,
                "jars" to jarsJson.toString()
            )
        }
    }
}
