package tech.medivh.plugin.gradle.publisher.setting

import org.gradle.api.publish.maven.MavenPomScm


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
data class Scm(private val remoteUrl: String) : SettingMan<MavenPomScm> {

    override fun setting(s: MavenPomScm) {
        s.connection.set("scm:git:$remoteUrl.git")
        s.url.set(remoteUrl)
    }

}
