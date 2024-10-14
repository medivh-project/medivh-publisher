plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradle.plugin-publish") version "1.3.0"
    `java-gradle-plugin`
}

group = "tech.medivh"
version = "0.0.1"

repositories {
    mavenCentral()
}



dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202308070828-r")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
