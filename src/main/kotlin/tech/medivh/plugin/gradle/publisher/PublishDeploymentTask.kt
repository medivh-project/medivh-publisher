package tech.medivh.plugin.gradle.publisher

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import tech.medivh.plugin.gradle.publisher.api.DeploymentState
import tech.medivh.plugin.gradle.publisher.api.SonatypeApi

/**
 * @author gongxuanzhangmelt@gmail.com
 */
open class PublishDeploymentTask : DefaultTask() {

    @get:Input
    var id: String = ""


    @TaskAction
    fun publishToSonatype() {
        if (project.hasProperty("deploymentId")) {
            id = project.property("deploymentId") as String
        }
        println("wait deploymentState...The process depends on the sonatype service speed")
        var deploymentState = SonatypeApi.deploymentState(id)
        while (deploymentState != DeploymentState.FAILED && deploymentState != DeploymentState.VALIDATED) {
            println("current deploymentState=$deploymentState")
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1))
            deploymentState = SonatypeApi.deploymentState(id)
        }
        require(deploymentState != DeploymentState.FAILED) {
            "deployment failed, you can check it in https://central.sonatype.com/publishing/deployments"
        }
        SonatypeApi.publish(id)
        println("publish success, you can check it in https://central.sonatype.com/publishing/deployments")

    }

}
