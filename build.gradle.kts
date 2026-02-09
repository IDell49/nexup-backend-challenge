plugins {
    // 1. Tell Gradle we are using Kotlin for JVM
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    // 2. Where to download libraries from
    mavenCentral()
}

dependencies {
    // 3. Testing framework (using Kotlin Test / JUnit 5)
    testImplementation(kotlin("test"))
}

tasks.test {
    // 4. Enables JUnit 5 for running tests
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main { kotlin { setSrcDirs(listOf("src")) } }
    test { kotlin { setSrcDirs(listOf("test")) } }
}