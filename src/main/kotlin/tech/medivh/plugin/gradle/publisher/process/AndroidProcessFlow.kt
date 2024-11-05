package tech.medivh.plugin.gradle.publisher.process

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import tech.medivh.plugin.gradle.publisher.MedivhPublisherExtension


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class AndroidProcessFlow : ProcessFlow {

    override fun fillDefaultValues(ext: MedivhPublisherExtension) {
        ext.publicationName = "MedivhMavenAndroid"
    }

    override fun setDocAndSources(project: Project, medivhExt: MedivhPublisherExtension) {
        //   Not supported yet
    }


    override fun setArtifacts(mavenPublication: MavenPublication, project: Project) {
        mavenPublication.artifact(
            project.layout.buildDirectory.dir("outputs/aar/${project.name}-release.aar").get().asFile.path
        )
    }

    override fun defaultGroupId(mavenPublication: MavenPublication, project: Project): String {
        val androidExtension = project.extensions.findByName("android") ?: return project.group.toString()
        val namespaceProperty = androidExtension::class.java.methods.find { it.name == "getNamespace" }
        val namespace = namespaceProperty?.invoke(androidExtension) as? String
        return namespace ?: project.group.toString()
    }

    override fun signDependsOn(): List<String> {
        return listOf("bundleReleaseAar")
    }
}