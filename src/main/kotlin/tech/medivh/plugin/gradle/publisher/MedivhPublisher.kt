package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class MedivhPublisher : Plugin<Project> {

    override fun apply(project: Project) {
        val medivhExtension = project.extensions.create("medivhPublisher", MedivhPublisherExtension::class.java)

        generateMavenPublishIfNecessary(project, medivhExtension)

        project.afterEvaluate {
            project.extensions.getByType(JavaPluginExtension::class.java).apply {
                if (medivhExtension.hasJavaDoc) {
                    withJavadocJar()
                }
                if (medivhExtension.hasSources) {
                    withSourcesJar()
                }
            }
        }

    }

    private fun generateMavenPublishIfNecessary(project: Project, medivhExtension: MedivhPublisherExtension) {
        if (project.plugins.hasPlugin(MavenPublishPlugin::class.java)) {
            return
        }
        project.plugins.apply(MavenPublishPlugin::class.java)

        project.extensions.configure(PublishingExtension::class.java) { publishing ->

            val generator = MedivhGenerator(project)

            publishing.repositories.maven { maven ->
                maven.name = "sonatype"
                maven.url = project.uri(project.layout.buildDirectory.dir("medivhRepo"))
            }

            publishing.publications.create("mavenJava", MavenPublication::class.java) { publication ->
                generator.generateMavenPublication(publication)
            }
        }

    }
}
