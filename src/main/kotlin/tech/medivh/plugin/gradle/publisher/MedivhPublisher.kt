package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import tech.medivh.plugin.gradle.publisher.api.SonatypeApi


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class MedivhPublisher : Plugin<Project> {

    override fun apply(project: Project) {
        project.run {
            dependencies.add("implementation", "com.squareup.okhttp3:okhttp:4.12.0")
            dependencies.add("implementation", "org.eclipse.jgit:org.eclipse.jgit:7.0.0.202409031743-r")
            dependencies.add("implementation", "com.alibaba.fastjson2:fastjson2:2.0.53")
            val medivhExtension = extensions.create("medivhPublisher", MedivhPublisherExtension::class.java, project)

            afterEvaluate {
                SonatypeApi.init(medivhExtension)
                generateMavenPublishIfNecessary(this, medivhExtension)
                generateSigningIfNecessary(this)

                extensions.getByType(JavaPluginExtension::class.java).apply {
                    if (medivhExtension.hasJavaDoc) {
                        withJavadocJar()
                    }
                    if (medivhExtension.hasSources) {
                        withSourcesJar()
                    }
                }
                tasks.register("cleanBuildMavenRepo") {
                    it.doLast {
                        project.file(medivhExtension.buildMavenRepo).deleteRecursively()
                    }
                }

                tasks.register("uploadBuildToSonatype", UploadSonatypeTask::class.java, medivhExtension).configure {
                    it.dependsOn("cleanBuildMavenRepo")
                    project.extensions.getByType(PublishingExtension::class.java).repositories.forEach { repository ->
                        val repositoryName = repository.name.replaceFirstChar { name ->
                            if (name.isLowerCase()) name.titlecase() else name.toString()
                        }
                        it.dependsOn("publishAllPublicationsTo${repositoryName}Repository")
                    }
                }
                tasks.register("publishDeployment", PublishDeploymentTask::class.java).configure {
                    it.dependsOn("uploadBuildToSonatype")
                }
            }
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
                maven.url = project.uri(medivhExtension.buildMavenRepo)
            }

            publishing.publications.create("medivhMavenJava", MavenPublication::class.java) { publication ->
                generator.generateMavenPublication(publication)
            }
        }
    }
}
