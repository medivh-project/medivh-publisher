plugins {
    kotlin("jvm") version "2.0.20"
    `java-gradle-plugin`
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(gradleTestKit())
}


tasks.named<Test>("test") {
    useJUnitPlatform()
}

