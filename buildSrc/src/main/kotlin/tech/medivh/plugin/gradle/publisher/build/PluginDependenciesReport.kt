package tech.medivh.plugin.gradle.publisher.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
open class PluginDependenciesReport : DefaultTask() {


    @TaskAction
    fun reportPluginDependencies() {
    }
}
