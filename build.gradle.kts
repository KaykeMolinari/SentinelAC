plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "com.sentinel"
version = "0.1.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("com.github.retrooper:packetevents-spigot:2.11.2")
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("com.github.retrooper.packetevents", "com.sentinel.ac.libs.packetevents.api")
        relocate("io.github.retrooper.packetevents", "com.sentinel.ac.libs.packetevents.impl")
    }
    build {
        dependsOn(shadowJar)
    }
}
