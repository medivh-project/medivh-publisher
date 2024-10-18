package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import javax.inject.Inject


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
open class MedivhPublisherExtension @Inject constructor(val project: Project) {

    val tempPublicationName = "MedivhMavenJavaTemp"

    val publicationName = "MedivhMavenJava"

    val repositoriesMavenName = "medivh_sonatype"

    val cleanTaskName = "cleanBuildMedivhMavenRepo"

    val taskGroup = "medivh-publish"
    
    val uploadTaskName = "uploadToSonatype"
    
    val generateTaskName = "generateMedivhPublication"

    lateinit var buildMavenRepo: String

    lateinit var uploadMavenRepo: String

    var hasJavaDoc = true

    var hasSources = true

    var pom: Action<MavenPom>? = null

    lateinit var sonatypeUsername: String

    lateinit var sonatypePassword: String

    var groupId: String? = null

    var artifactId: String? = null

    var version: String? = null

    internal fun fillAfterEvaluate() {
        if (!::buildMavenRepo.isInitialized) {
            buildMavenRepo = project.layout.buildDirectory.dir("medivhRepo").get().asFile.absolutePath
        }
        if (!::uploadMavenRepo.isInitialized) {
            uploadMavenRepo = project.layout.buildDirectory.dir("sonatypeUpload").get().asFile.absolutePath
        }
        if (this.groupId == null) {
            groupId = project.group.toString()
        }
        if (this.artifactId == null) {
            artifactId = project.name
        }
        if (this.version == null) {
            version = project.version.toString()
        }
        if (!::sonatypeUsername.isInitialized) {
            sonatypeUsername = project.findProperty("sonatypeUsername")?.toString()
                ?: throw IllegalStateException("sonatypeUsername is null")
        }
        if (!::sonatypePassword.isInitialized) {
            sonatypePassword = project.findProperty("sonatypePassword")?.toString()
                ?: throw IllegalStateException("sonatypePassword is null")
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

}
