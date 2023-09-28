plugins {
    `java-library`

    id("io.papermc.paperweight.userdev") version "1.5.5"
}

dependencies {
    // 1.17 is not supported by paperweight, but 1.17.1 is still R1
    paperweight.paperDevBundle("1.17.1-R0.1-SNAPSHOT")

    compileOnly(project(":api"))
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
}
