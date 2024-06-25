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

    // Shades and relocates dependencies into our plugin jar. See https://imperceptiblethoughts.com/shadow/introduction/
    id("io.github.goooler.shadow") version "8.1.7"
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")

    // Shadow will include the runtimeClasspath by default, which implementation adds to.
    // Dependencies you don't want to include go in the compileOnly configuration.
    // Make sure to relocate shaded dependencies!
    implementation(project(":api"))
    implementation(project(":v1_20_5", "reobf"))
    implementation(project(":v1_20_3", "reobf"))
    implementation(project(":v1_20_2", "reobf"))
    implementation(project(":v1_20", "reobf"))
    implementation(project(":v1_19_4", "reobf"))
    implementation(project(":v1_19_3", "reobf"))
    implementation(project(":v1_19", "reobf"))
    implementation(project(":v1_18_2", "reobf"))
    implementation(project(":v1_18", "reobf"))
    implementation(project(":v1_17", "reobf"))

    implementation("com.github.retrooper:packetevents-spigot:2.3.1-SNAPSHOT")
    implementation("dev.jorel:commandapi-bukkit-shade:9.5.1")
    implementation("net.kyori:adventure-platform-bukkit:4.3.3")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
                "name" to rootProject.name,
                "version" to rootProject.version,
                "description" to rootProject.description,
                "apiVersion" to "1.17"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveBaseName.set("pvpclub-${rootProject.name}")
        archiveClassifier.set("")
        archiveVersion.set("${rootProject.version}")

        minimize()
        relocate("com.github.retrooper.packetevents", "xyz.reknown.fastercrystals.packetevents.api")
        relocate("io.github.retrooper.packetevents", "xyz.reknown.fastercrystals.packetevents.impl")
        relocate("dev.jorel.commandapi", "xyz.reknown.fastercrystals.commandapi")
        relocate("net.kyori", "xyz.reknown.fastercrystals.kyori")

        from("../LICENSE")
    }
}
