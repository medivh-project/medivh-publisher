package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom
import org.gradle.plugins.signing.SigningExtension
import javax.inject.Inject


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
open class MedivhPublisherExtension @Inject constructor(val project: Project) {

    val repositoriesMavenName = "medivhSonatype"

    val cleanTaskName = "cleanBuildMedivhMavenRepo"

    val taskGroup = "medivh-publish"

    var publicationName = "MedivhMavenJava"

    val uploadTaskName = "uploadToSonatype"

    lateinit var buildMavenRepo: String

    lateinit var uploadMavenRepo: String

    var hasJavaDoc = true

    var hasSources = true

    var pom: Action<MavenPom>? = null

    lateinit var sonatypeUsername: String

    lateinit var sonatypePassword: String

    internal lateinit var finalUploadName: String

    var groupId: String? = null

    var artifactId: String? = null

    var version: String? = null
    
    var repositoriesAction: Action<RepositoryHandler>? = null

    internal fun fillAfterEvaluate() {
        if (!::buildMavenRepo.isInitialized) {
            buildMavenRepo = project.layout.buildDirectory.dir("medivhRepo").get().asFile.absolutePath
        }
        if (!::uploadMavenRepo.isInitialized) {
            uploadMavenRepo = project.layout.buildDirectory.dir("sonatypeUpload").get().asFile.absolutePath
        }
        if (!::sonatypeUsername.isInitialized) {
            sonatypeUsername = project.findProperty("sonatypeUsername")?.toString() ?: ""
        }
        if (!::sonatypePassword.isInitialized) {
            sonatypePassword = project.findProperty("sonatypePassword")?.toString() ?: ""
        }
    }

    fun withoutJavaDocJar() {
        hasJavaDoc = false
    }

    fun withoutSourcesJar() {
        hasSources = false
    }

    fun pom(pomAction: Action<MavenPom>) {
        this.pom = pomAction
    }
    
    fun repositories(repositoriesAction: Action<RepositoryHandler>) {
        this.repositoriesAction = repositoriesAction
    }

    fun signing(configuration: SigningExtension.() -> Unit) {
        project.afterEvaluate {
            val signingExtension = project.extensions.findByName("signing") as? SigningExtension
                ?: throw IllegalStateException("Target plugin 'signing' is not applied!")
            signingExtension.configuration()
        }

    }

}
