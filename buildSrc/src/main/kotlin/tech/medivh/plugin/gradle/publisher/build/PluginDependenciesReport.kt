package tech.medivh.plugin.gradle.publisher.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class PluginDependenciesReport : DefaultTask() {


    @TaskAction
    fun reportPluginDependencies() {
        group = "build"
        description = "report plugin dependencies"
        doLast{
            project.configurations.forEach { configuration ->
                println("${configuration.name}:")
                configuration.dependencies.forEach {
                    println("  ${it.group}:${it.name}:${it.version}")
                }
            }
        }
    }
}
