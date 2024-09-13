plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.1"
}

group = "io.github.kabanfriends"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.apache.pdfbox:fontbox:3.0.3")
    implementation("commons-io:commons-io:2.16.1")
    implementation("com.zaxxer:nuprocess:2.0.6")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "io.github.kabanfriends.smoothfontgen.SmoothFontGen"
        }
    }

    build {
        dependsOn("shadowJar")
    }
}
