plugins {
    `java-library`

    id("io.papermc.paperweight.userdev") version "1.6.3"
}

dependencies {
    paperweight.paperDevBundle("1.18.2-R0.1-SNAPSHOT")

    compileOnly(project(":api"))
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}
