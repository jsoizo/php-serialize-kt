plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    `java-library`
}

group = "com.jsoizo"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val kotestVersion = "5.6.2"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}