rootProject.name = "medivh-publisher"

val versions = java.util.Properties().apply {
    file("versions.properties").inputStream().use { load(it) }
}
gradle.extra["versions"] = versions
