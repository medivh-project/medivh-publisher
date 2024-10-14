package tech.medivh.plugin.gradle.publisher.setting

import org.gradle.api.publish.maven.MavenPomDeveloper


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
data class Developer(val name: String, val email: String) : SettingMan<MavenPomDeveloper> {

    override fun setting(s: MavenPomDeveloper) {
        s.name.set(name)
        s.email.set(email)
        s.id.set(name)
    }

}
