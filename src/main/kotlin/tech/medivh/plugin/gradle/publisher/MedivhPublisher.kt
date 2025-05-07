package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import tech.medivh.plugin.gradle.publisher.api.SonatypeApi
import tech.medivh.plugin.gradle.publisher.process.AndroidProcessFlow
import tech.medivh.plugin.gradle.publisher.process.JavaProcessFlow
import tech.medivh.plugin.gradle.publisher.process.ProcessFlow


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class MedivhPublisher : Plugin<Project> {

    override fun apply(project: Project) {
        project.run {
            val medivhExt = extensions.create("medivhPublisher", MedivhPublisherExtension::class.java, project)
            afterEvaluate {
                gradleProject = project

                SonatypeApi.init(medivhExt)

                applyRequiredPlugins(project)

                val processFlow = selectProcessFlow(project)

                processFlow.fillDefaultValues(medivhExt)

                setMedivhMavenRepo(project, medivhExt)

                processFlow.setDocAndSources(project, medivhExt)

                registerCleanTask(project, medivhExt)

                generateMavenPublication(project, processFlow)

                generateSigningIfNecessary(project)

                registerUploadTask(project, medivhExt)

                processFlow.signDependsOn().forEach { taskName ->
                    tasks.named("sign${medivhExt.publicationName.uppercaseFirstChar()}Publication").configure {
                        it.dependsOn(taskName)
                    }
                }

                tasks.register("publishDeployment", PublishDeploymentTask::class.java).configure {
                    it.group = medivhExt.taskGroup
                    it.dependsOn(medivhExt.uploadTaskName)
                }
            }
        }
    }

    private fun selectProcessFlow(project: Project): ProcessFlow {
        if (project.plugins.hasPlugin("android-library")) {
            return AndroidProcessFlow()
        }
        if (project.plugins.hasPlugin(JavaBasePlugin::class.java)) {
            return JavaProcessFlow()
        }
        throw IllegalStateException("Unsupported plugin")
    }


    private fun generateMavenPublication(project: Project, processFlow: ProcessFlow) {
        val generator = MedivhGenerator(project, processFlow)
        generator.generateMavenPublication()
    }

    private fun registerUploadTask(project: Project, medivhExt: MedivhPublisherExtension) {
        project.tasks.register(medivhExt.uploadTaskName, UploadSonatypeTask::class.java, medivhExt).configure { task ->
            task.group = medivhExt.taskGroup
            task.dependsOn(medivhExt.cleanTaskName)
            task.dependsOn("publish${medivhExt.publicationName.uppercaseFirstChar()}PublicationTo${medivhExt.repositoriesMavenName.uppercaseFirstChar()}Repository")
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
        project.extensions.configure(SigningExtension::class.java) { signing ->
            val publishing = project.extensions.getByType(PublishingExtension::class.java)
            val medivhPublication = publishing.publications.getByName(project.medivhPublisherExtension.publicationName)
            signing.sign(medivhPublication)
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
