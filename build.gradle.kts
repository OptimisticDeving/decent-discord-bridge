import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.BufferedOutputStream
import java.nio.file.Files
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

plugins {
    kotlin("jvm") version "2.0.0"
    id("fabric-loom") version "1.7-SNAPSHOT"
}

loom {
    serverOnlyMinecraftJar()
}

group = "dev.optimistic"
version = "1.4.0"

val jij: Configuration by configurations.creating

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.11")

    // config
    jij("org.spongepowered:configurate-core:4.1.2")
    jij("org.spongepowered:configurate-extra-kotlin:4.1.2")
    jij("me.lucko.configurate:configurate-toml:4.1")

    // discord
    jij("net.dv8tion:JDA:5.0.0-beta.24") {
        exclude(module = "opus-java")
    }
    jij("club.minnced:jda-ktx:0.11.0-beta.20") {
        exclude(module = "JDA")
    }
    jij("club.minnced:discord-webhooks:0.8.4")
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
