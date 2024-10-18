package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskProvider
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
            val medivhExt = extensions.create("medivhPublisher", MedivhPublisherExtension::class.java, project)
            afterEvaluate {
                gradleProject = project

                SonatypeApi.init(medivhExt)

                applyRequiredPlugins(project)

                setMedivhMavenRepo(project, medivhExt)

                setJavaDocAndSources(project, medivhExt)

                registerCleanTask(project, medivhExt)
                
                generateMavenPublication(project)

                createTempPublication(project, medivhExt)

                registerUploadTask(project, medivhExt)

                tasks.register("publishDeployment", PublishDeploymentTask::class.java).configure {
                    group = medivhExt.taskGroup
                    it.dependsOn(medivhExt.uploadTaskName)
                }
            }
        }
    }

    private fun generateMavenPublication(project: Project) {
        val generator = MedivhGenerator(project)
        generator.generateMedivhMavenPublication()
    }

    private fun createTempPublication(project: Project, medivhExt: MedivhPublisherExtension) {
        val publications = project.extensions.getByType(PublishingExtension::class.java).publications
        publications.create(medivhExt.tempPublicationName, MavenPublication::class.java) {
            medivhExt.pom?.execute(it.pom)
        }
    }

    private fun registerUploadTask(project: Project, medivhExt: MedivhPublisherExtension) {
        project.tasks.register(medivhExt.uploadTaskName, UploadSonatypeTask::class.java, medivhExt).configure {
            it.group = medivhExt.taskGroup
            it.dependsOn(medivhExt.generateTaskName)
//            it.dependsOn("publish${medivhExt.publicationName.uppercaseFirstChar()}To${medivhExt.repositoriesMavenName.uppercaseFirstChar()}Repository")
            it.doLast {
                println("upload!!!!!")
            }
        }
    }

    private fun registerCleanTask(project: Project, medivhExt: MedivhPublisherExtension): TaskProvider<Task> {
        return project.tasks.register(medivhExt.cleanTaskName) {
            it.group = medivhExt.taskGroup
            it.doLast {
                project.file(medivhExt.buildMavenRepo).apply {
                    if (exists()) {
                        this.deleteRecursively()
                    }
                }
            }
        }
    }

    private fun setJavaDocAndSources(project: Project, medivhExt: MedivhPublisherExtension) {
        project.extensions.getByType(JavaPluginExtension::class.java).apply {
            if (medivhExt.hasJavaDoc) {
                withJavadocJar()
            }
            if (medivhExt.hasSources) {
                withSourcesJar()
            }
        }
    }

    private fun setMedivhMavenRepo(project: Project, medivhExtension: MedivhPublisherExtension) {

        project.extensions.configure(PublishingExtension::class.java) {
            it.repositories.maven { maven ->
                maven.name = medivhExtension.repositoriesMavenName
                maven.url = project.uri(medivhExtension.buildMavenRepo)
            }
        }
    }

    private fun applyRequiredPlugins(project: Project) {
        requiredPlugins.forEach { requiredPlugin ->
            if (!project.plugins.hasPlugin(requiredPlugin)) {
                project.plugins.apply(requiredPlugin)
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

    private fun String.uppercaseFirstChar() = replaceFirstChar { it.uppercase() }

    companion object {
        val requiredPlugins = listOf(
            SigningPlugin::class.java,
            MavenPublishPlugin::class.java
        )
    }
}
