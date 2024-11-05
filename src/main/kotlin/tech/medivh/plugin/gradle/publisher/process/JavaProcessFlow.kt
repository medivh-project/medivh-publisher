package tech.medivh.plugin.gradle.publisher.process

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.maven.MavenPublication
import tech.medivh.plugin.gradle.publisher.MedivhPublisherExtension


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class JavaProcessFlow : ProcessFlow {

    override fun fillDefaultValues(ext: MedivhPublisherExtension) {
        ext.publicationName = "MedivhMavenJava"
    }

    override fun setDocAndSources(project: Project, medivhExt: MedivhPublisherExtension) {
        project.extensions.getByType(JavaPluginExtension::class.java).apply {
            if (medivhExt.hasJavaDoc) {
                withJavadocJar()
            }
            if (medivhExt.hasSources) {
                withSourcesJar()
            }
        }
    }

    override fun setArtifacts(mavenPublication: MavenPublication, project: Project) {
        mavenPublication.from(project.components.getByName("java"))
    }

    override fun defaultGroupId(mavenPublication: MavenPublication, project: Project): String {
        return project.group.toString()
    }

}