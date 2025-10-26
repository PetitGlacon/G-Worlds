plugins {
    java
    `maven-publish`
    id ("io.github.goooler.shadow") version "8.1.8"
    id ("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

group = "fr.The__Glacier"
version = "0.0.1-SNAPSHOT-raw"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/" )
    // maven("https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/")
    maven {
        url = uri("https://jitpack.io")
//        content {
//            includeGroup("com.github.PetitGlacon")
//        }
    }
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    paperweight.foliaDevBundle("1.21.8-R0.1-SNAPSHOT")
    // implementation("lien") -> inclu le lien dans le projet
    // compileOnly("lien") -> ajoute un projet en dépendance (pas inclu dans le jar final)
    compileOnly("com.github.PetitGlacon:G-Core:snapshot-SNAPSHOT")
    // compileOnly(files("D:\\Dev\\Project\\G-Plugins\\G-Core\\G-Core\\build\\libs\\G-Core-1.0.0-SNAPSHOT.jar"))
    compileOnly("org.projectlombok:lombok:1.18.30")
    // compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // plugin dependencies

    annotationProcessor("org.projectlombok:lombok:1.18.30")
}


configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks {
    jar {
        // dependsOn(shadowJar)
        enabled = false
    }

    shadowJar {
        archiveBaseName.set("G-Worlds")
        archiveClassifier.set("")
        archiveVersion.set(version.toString().replace("-raw", ""))

        relocate("com.fasterxml", "fr.the__glacier.dependencies.fasterxml")
        relocate("de.tr7zw", "fr.the__glacier.dependencies.tr7zw")
    }

    compileJava {
        options.encoding = "UTF-8"
    }
}
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            // from(components["java"])

            groupId = "fr.The__Glacier"
            artifactId = "G-Worlds"
            version = "1.0.0"

            artifact(tasks["shadowJar"]) {
                classifier = ""  // Pas de classifier pour éviter des artefacts multiples
            }
        }
    }
    repositories {
        mavenLocal() // Cela publiera l'artefact dans ton dépôt Maven local
    }
}