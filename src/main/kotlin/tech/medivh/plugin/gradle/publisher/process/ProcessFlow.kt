package tech.medivh.plugin.gradle.publisher.process

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import tech.medivh.plugin.gradle.publisher.MedivhPublisherExtension


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
interface ProcessFlow {

    /**
     * Fill default values
     */
    fun fillDefaultValues(ext: MedivhPublisherExtension)

    /**
     * publish contains java sources and java doc
     */
    fun setDocAndSources(project: Project, medivhExt: MedivhPublisherExtension)

    /**
     * set artifacts
     */
    fun setArtifacts(mavenPublication: MavenPublication, project: Project)

    /**
     * result will set to maven publication groupId when groupId is null
     */
    fun defaultGroupId(mavenPublication: MavenPublication, project: Project): String


    fun signDependsOn(): List<String> = emptyList()


}