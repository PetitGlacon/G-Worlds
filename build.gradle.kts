plugins {
    java
    `maven-publish`
    id ("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.4"
}

group = "fr.the__glacier"
version = "1.0-SNAPSHOT-raw"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://github.com/Folia-Inquisitors/FoliaDevBundle/raw/gh-pages/")
}

dependencies {
    paperweight.foliaDevBundle("1.21.1-R0.1-SNAPSHOT")
    compileOnly(files("D://Dev/Project/G-Plugins/G-Core/G-Core/build/libs/G-Core-1.0.0-SNAPSHOT.jar"))
    // compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.30")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks {
    jar {
        dependsOn(shadowJar)
        enabled = true
    }

    shadowJar {
        archiveBaseName.set("G-Worlds")
        archiveClassifier.set("")
        archiveVersion.set(version.toString().replace("-raw", ""))
    }

    compileJava {
        options.encoding = "UTF-8"
    }
}
