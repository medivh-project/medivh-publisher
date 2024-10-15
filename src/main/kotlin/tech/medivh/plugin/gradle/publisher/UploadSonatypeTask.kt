package tech.medivh.plugin.gradle.publisher

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import tech.medivh.plugin.gradle.publisher.api.SonatypeApi
import tech.medivh.plugin.gradle.publisher.api.zipFolder
import javax.inject.Inject

/**
 * @author gongxuanzhangmelt@gmail.com
 */
open class UploadSonatypeTask @Inject constructor(@Input val medivh: MedivhPublisherExtension) : DefaultTask() {

    @TaskAction
    fun uploadToSonatype() {
        val mavenBuildDir = File(medivh.buildMavenRepo)
        val uploadDir = File(medivh.uploadMavenRepo)
        val uploadFile = File(uploadDir, "upload.zip")
        zipFolder(mavenBuildDir, uploadFile)
        val deploymentId = SonatypeApi.upload(uploadFile, medivh.artifactId)
        project.extensions.extraProperties["deploymentId"] = deploymentId
        println("upload success, deploymentId=$deploymentId , deploymentState=${SonatypeApi.deploymentState(deploymentId)}")
    }

}
