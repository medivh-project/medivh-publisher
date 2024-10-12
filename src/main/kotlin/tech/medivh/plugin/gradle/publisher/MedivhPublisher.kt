package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class MedivhPublisher : Plugin<Project> {
    
    override fun apply(project: Project) {
        project.extensions.add("medivhPublisher", MedivhPublisherExtension::class.java)
        
        
        
    }
}
