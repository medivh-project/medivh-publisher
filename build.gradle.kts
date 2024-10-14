plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradle.plugin-publish") version "1.3.0"
    `java-gradle-plugin`
}

group = "tech.medivh"
version = "0.0.1"

repositories {
    mavenLocal()
    mavenCentral()
}



dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.0.0.202409031743-r")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}


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
