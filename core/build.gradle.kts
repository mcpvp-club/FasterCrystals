/*
 * Copyright (C) 2023-2024 Jyguy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>. 
 */

plugins {
    `java-library`

    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
    id("com.gradleup.shadow") version "8.3.5"
}

version = rootProject.version

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("com.github.retrooper:packetevents-spigot:2.7.0-SNAPSHOT")
    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:9.7.0")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks {
    build {
        dependsOn("shadowJar")
    }
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    shadowJar {
        archiveBaseName.set("pvpclub-${rootProject.name}")
        archiveClassifier.set("")
        archiveVersion.set("${rootProject.version}")

        minimize()
        relocate("com.github.retrooper.packetevents", "xyz.reknown.fastercrystals.packetevents.api")
        relocate("io.github.retrooper.packetevents", "xyz.reknown.fastercrystals.packetevents.impl")
        relocate("dev.jorel.commandapi", "xyz.reknown.fastercrystals.commandapi")

        from("../LICENSE")
    }
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
    softDepend.addAll("PlaceholderAPI", "ProtocolLib", "ProtocolSupport", "ViaVersion", "ViaBackwards", "ViaRewind", "Geyser-Spigot")
}
