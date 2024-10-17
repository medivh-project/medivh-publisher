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
            dependencies.add("implementation", "com.alibaba.fastjson2:fastjson2:2.0.53")
            val medivhExt = extensions.create("medivhPublisher", MedivhPublisherExtension::class.java, project)
            afterEvaluate {
                SonatypeApi.init(medivhExt)

                applyRequiredPlugins(project)

                setMedivhMavenRepo(project, medivhExt)

                setJavaDocAndSources(project, medivhExt)

                registerCleanTask(project, medivhExt)

                val userMavenPublication = getUserMavenPublication(project)

                createTempPublication(project, medivhExt)

                println(userMavenPublication?.name)
                registerGenerateTaskName(medivhExt, userMavenPublication?.name)

                //   Some configurations are not available from the api and must be known after the pom is generated,
                //   such as developer information


                val generator = MedivhGenerator(project)

                //                generator.generateMedivhMavenPublication()

                //                generateMavenPublishIfNecessary(this, medivhExt)
                //                generateSigningIfNecessary(this)

                registerUploadTask(project, medivhExt)


                tasks.register("publishDeployment", PublishDeploymentTask::class.java).configure {
                    group = medivhExt.taskGroup
                    it.dependsOn(medivhExt.uploadTaskName)
                }
            }
        }
    }

    private fun getUserMavenPublication(project: Project): MavenPublication? {
        val publications = project.extensions.getByType(PublishingExtension::class.java).publications
        val userMavenPublication = publications.withType(MavenPublication::class.java)
        check(userMavenPublication.size < 2) { "you can't have more than one publication" }
        return userMavenPublication.firstOrNull()
    }

    private fun createTempPublication(project: Project, medivhExt: MedivhPublisherExtension) {
        val publications = project.extensions.getByType(PublishingExtension::class.java).publications
        publications.create(medivhExt.tempPublicationName, MavenPublication::class.java) {
            medivhExt.pom?.execute(it.pom)
        }
    }

    private fun registerGenerateTaskName(medivhExt: MedivhPublisherExtension, userMavenPublicationName: String?) {
        val project = medivhExt.project
        project.tasks.register(medivhExt.generateTaskName).configure {
            it.group = medivhExt.taskGroup
            it.dependsOn(medivhExt.cleanTaskName)
            it.dependsOn("generatePomFileFor${medivhExt.tempPublicationName}Publication")
            if (userMavenPublicationName != null) {
                it.dependsOn("generatePomFileFor${userMavenPublicationName}Publication")
            }
            it.doLast {
                val publicationsDir = project.layout.buildDirectory.dir("publications").get().asFile
                val tempPom = publicationsDir.resolve("/${medivhExt.tempPublicationName}/").resolve("pom-default.xml")
                val userPom = userMavenPublicationName?.run {
                    println(this)
                    publicationsDir.resolve("/$this/").resolve("pom-default.xml")
                }
                println(tempPom)
                println(userPom)
                val publishing = project.extensions.getByType(PublishingExtension::class.java)
                publishing.publications.create(medivhExt.publicationName, MavenPublication::class.java) { publication ->
                    println("success create medivh publication")
                }
            }
        }
    }

    private fun registerUploadTask(project: Project, medivhExt: MedivhPublisherExtension) {
        project.tasks.register(medivhExt.uploadTaskName, UploadSonatypeTask::class.java, medivhExt).configure {
            it.group = medivhExt.taskGroup
            //            project.extensions.getByType(PublishingExtension::class.java).repositories.forEach { repository ->
            //                val repositoryName = repository.name.replaceFirstChar { name ->
            //                    if (name.isLowerCase()) name.titlecase() else name.toString()
            //                }
            //                it.dependsOn("publishAllPublicationsTo${repositoryName}Repository")
            //            }
            it.dependsOn(medivhExt.generateTaskName)
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


    private fun generateMavenPublishIfNecessary(project: Project, medivhExtension: MedivhPublisherExtension) {
        if (project.plugins.hasPlugin(MavenPublishPlugin::class.java)) {
            return
        }
        project.plugins.apply(MavenPublishPlugin::class.java)

        project.extensions.configure(PublishingExtension::class.java) { publishing ->

            val generator = MedivhGenerator(project)

            publishing.repositories.maven { maven ->
                maven.name = medivhExtension.repositoriesMavenName
                maven.url = project.uri(medivhExtension.buildMavenRepo)
            }

            publishing.publications.create("medivh_temp_", MavenPublication::class.java) { publication ->
                generator.generateMavenPublication(publication)
            }
        }
    }

    companion object {
        val requiredPlugins = listOf(
            SigningPlugin::class.java,
            MavenPublishPlugin::class.java
        )
    }
}
