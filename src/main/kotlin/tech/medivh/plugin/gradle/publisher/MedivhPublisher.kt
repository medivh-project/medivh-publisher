package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class MedivhPublisher : Plugin<Project> {

    override fun apply(project: Project) {
        project.dependencies.add("implementation", "com.squareup.okhttp3:okhttp:4.12.0")
        project.dependencies.add("implementation", "org.eclipse.jgit:org.eclipse.jgit:7.0.0.202409031743-r")
        
        val medivhExtension = project.extensions.create("medivhPublisher", MedivhPublisherExtension::class.java)

        generateMavenPublishIfNecessary(project, medivhExtension)

        generateSigningIfNecessary(project)

        
        project.afterEvaluate {

            SonatypeApi.authToken = calcAuthToken(medivhExtension.tokenUsername, medivhExtension.tokenPassword)
            
            project.extensions.getByType(JavaPluginExtension::class.java).apply {
                if (medivhExtension.hasJavaDoc) {
                    withJavadocJar()
                }
                if (medivhExtension.hasSources) {
                    withSourcesJar()
                }
            }
            project.tasks.create("publishToSonatype", PublishToSonatypeTask::class.java, medivhExtension)
        }

    }

    private fun generateSigningIfNecessary(project: Project) {
        if (!project.plugins.hasPlugin(SigningPlugin::class.java)) {
            project.plugins.apply(SigningPlugin::class.java)
        }
        project.extensions.configure(SigningExtension::class.java) { signing ->
            val publishing = project.extensions.getByType(PublishingExtension::class.java)
            publishing.publications.forEach {
                signing.sign(it)
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

            publishing.publications.create("medivhMavenJava", MavenPublication::class.java) { publication ->
                generator.generateMavenPublication(publication)
            }
        }
    }
}
