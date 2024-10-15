package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Project
import javax.inject.Inject


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
open class MedivhPublisherExtension @Inject constructor(val project: Project) {

    lateinit var buildMavenRepo: String

    lateinit var uploadMavenRepo: String

    var hasJavaDoc = true

    var hasSources = true

    lateinit var sonatypeUsername: String

    lateinit var sonatypePassword: String

    lateinit var groupId: String

    lateinit var artifactId: String

    lateinit var version: String

    internal fun fillAfterEvaluate() {
        if (!::buildMavenRepo.isInitialized) {
            buildMavenRepo = project.layout.buildDirectory.dir("medivhRepo").get().asFile.absolutePath
        }
        if (!::uploadMavenRepo.isInitialized) {
            uploadMavenRepo = project.layout.buildDirectory.dir("sonatypeUpload").get().asFile.absolutePath
        }
        if (!::groupId.isInitialized) {
            groupId = project.group.toString()
        }
        if (!::artifactId.isInitialized) {
            artifactId = project.name
        }
        if (!::version.isInitialized) {
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

}
