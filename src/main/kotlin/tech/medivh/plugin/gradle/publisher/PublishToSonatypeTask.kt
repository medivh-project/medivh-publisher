package tech.medivh.plugin.gradle.publisher

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * @author gongxuanzhangmelt@gmail.com
 */
open class PublishToSonatypeTask@Inject constructor(@Input val medivh: MedivhPublisherExtension) : DefaultTask() {

    @TaskAction
    fun publishToSonatype() {
        dependsOn("publishAllPublicationsToMavenRepository")
        zipFolder(File(medivh.dir), "medivh.zip")
    }

}
