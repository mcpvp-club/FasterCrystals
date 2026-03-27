plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory.bukkit.convention)
    alias(libs.plugins.paperweight.userdev)
}

group = "xyz.reknown.fastercrystals"
version = "2.2.0"
description = "Uses packets to manually break/place crystals"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    disableAutoTargetJvm()
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.get())

    compileOnly(libs.packetevents.spigot)
    compileOnly(libs.placeholderapi)

    implementation(libs.bstats.bukkit)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveFileName = "${rootProject.name}-${version}.jar"
        archiveClassifier = null

        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }

        from(rootProject.file("LICENSE")) {
            into("")
        }

        relocate("org.bstats", "xyz.reknown.fastercrystals.libs.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }

    runServer {
        val mcVersion = libs.versions.paper.get().split("-")[0]
        minecraftVersion(mcVersion)
        runDirectory = rootDir.resolve("run/paper/$mcVersion")

        downloadPlugins {
            url("https://cdn.modrinth.com/data/HYKaKraK/versions/YjTc55NR/packetevents-spigot-2.11.2.jar")
            url("https://github.com/ViaVersion/ViaVersion/releases/download/5.8.0/ViaVersion-5.8.0.jar")
            url("https://github.com/ViaVersion/ViaBackwards/releases/download/5.8.0/ViaBackwards-5.8.0.jar")
            url("https://github.com/PlaceholderAPI/PlaceholderAPI/releases/download/2.12.2/PlaceholderAPI-2.12.2.jar")
        }

        jvmArgs = listOf(
            "-Dcom.mojang.eula.agree=true",
            "-DPaper.IgnoreJavaVersion=true"
        )
    }

    runPaper.folia.registerTask()
}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.
bukkitPluginYaml {
    name = "FasterCrystals"
    main = "xyz.reknown.fastercrystals.FasterCrystals"
    version = project.version.toString()
    authors.add("Jyguy")
    apiVersion = "1.20.5"
    foliaSupported = true
    depend.addAll("packetevents")
    softDepend.addAll("PlaceholderAPI")
    commands.register("fastercrystals") {
        usage = "/fastercrystals <on/off|toggle>"
    }
}
