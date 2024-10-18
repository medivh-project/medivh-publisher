package tech.medivh.plugin.gradle.publisher.setting

import org.gradle.api.publish.maven.MavenPomScm
import tech.medivh.plugin.gradle.publisher.gradleProject


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class Scm(remoteUrl: String? = null) : SettingMan<MavenPomScm> {

    var connection: String? = null

    var url: String? = null

    init {
        url = remoteUrl
        if (remoteUrl != null) {
            connection = "scm:git:$remoteUrl"
        }
    }

    override fun setting(s: MavenPomScm) {
        s.connection.set(connection ?: run {
            gradleProject.logger.warn("scm.connection will set empty,because it is null")
            ""
        })
        s.url.set(url ?: run {
            gradleProject.logger.warn("scm.url will set empty,because it is null")
            ""
        })
    }

}
