package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
val Project.userMavenPublication: MavenPublication?
    get() {
        val publications = extensions.getByType(PublishingExtension::class.java).publications
        publications.withType(MavenPublication::class.java)
            .filter { it.name != medivhPublisherExtension.tempPublicationName && it.name != medivhPublisherExtension.publicationName }
            .apply {
                check(size < 2) { "you can't have more than one publication" }
                return firstOrNull()
            }
    }

val Project.medivhTempPublication: MavenPublication
    get() {
        val publications = extensions.getByType(PublishingExtension::class.java).publications
        return publications.withType(MavenPublication::class.java)
            .first { it.name == medivhPublisherExtension.tempPublicationName }
    }

val Project.medivhPublisherExtension: MedivhPublisherExtension
    get() = extensions.getByType(MedivhPublisherExtension::class.java)

/**
 * global gradle project
 */
lateinit var gradleProject: Project
