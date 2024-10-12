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
    testImplementation(kotlin("test"))
}

val ktor_version = "2.3.4"  // 确保使用最新版本

dependencies {
    implementation("io.ktor:ktor-client-core:$ktor_version")  // 核心库
    implementation("io.ktor:ktor-client-cio:$ktor_version")   // CIO 引擎
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
