import org.jetbrains.kotlin.gradle.plugin.extraProperties

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradle.plugin-publish") version "1.3.0"
    `java-gradle-plugin`
}

group = "tech.medivh"
version = "1.2.2"

repositories {
    mavenLocal()
    mavenCentral()
}


dependencies {
    val versions = gradle.extraProperties["versions"] as java.util.Properties
    val jacksonVersion: String by versions
    val okhttpVersion: String by versions
    val jgitVersion: String by versions
    implementation("com.squareup.okhttp3:okhttp:${okhttpVersion}")
    implementation("org.eclipse.jgit:org.eclipse.jgit:${jgitVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${jacksonVersion}")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}


kotlin {
    jvmToolchain(17)
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    website = "https://medivh.tech"
    vcsUrl = "https://github.com/medivh-project/medivh-publisher.git"
    plugins {
        create("medivh-publisher") {
            id = "tech.medivh.plugin.publisher"
            implementationClass = "tech.medivh.plugin.gradle.publisher.MedivhPublisher"
            group = "tech.medivh"
            version = project.version.toString()
            displayName = "sonatype publisher"
            description = "a gradle plugin , all in one publishing"
            tags = setOf("maven", "sonatype", "publisher")
        }
    }
}
