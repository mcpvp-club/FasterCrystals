plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("xyz.jpenilla.run-paper") version "2.1.0" // Adds runServer and runMojangMappedServer tasks for testing

    // Shades and relocates dependencies into our plugin jar. See https://imperceptiblethoughts.com/shadow/introduction/
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "xyz.reknown.fastercrystals"
version = "1.0.0"
description = "Uses packets to manually break/place crystals"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")

    // Shadow will include the runtimeClasspath by default, which implementation adds to.
    // Dependencies you don't want to include go in the compileOnly configuration.
    // Make sure to relocate shaded dependencies!
    implementation("com.github.retrooper.packetevents:spigot:2.0.2")
    implementation("dev.jorel:commandapi-bukkit-shade:9.1.0")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

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
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.20"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    /*
    reobfJar {
      // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
      // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
      outputJar.set(layout.buildDirectory.file("libs/PaperweightTestPlugin-${project.version}.jar"))
    }
     */

    shadowJar {
        minimize()
        relocate("com.github.retrooper.packetevents", "xyz.reknown.fastercrystals.packetevents.api")
        relocate("io.github.retrooper.packetevents", "xyz.reknown.fastercrystals.packetevents.impl")
        relocate("dev.jorel.commandapi", "xyz.reknown.fastercrystals.commandapi")
    }
}
