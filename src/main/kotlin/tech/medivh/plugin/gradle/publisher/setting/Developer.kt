package tech.medivh.plugin.gradle.publisher.setting

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.gradle.api.publish.maven.MavenPomDeveloper
import tech.medivh.plugin.gradle.publisher.gradleProject


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
class Developer : SettingMan<MavenPomDeveloper> {

    var id: String? = null

    var name: String? = null

    var email: String? = null

    override fun setting(s: MavenPomDeveloper) {
        s.name.set(name ?: run {
            gradleProject.logger.warn("developer.name will set empty,because it is null")
            ""
        })
        s.email.set(email ?: run {
            gradleProject.logger.warn("developer.email will set empty,because it is null")
            ""
        })
        s.id.set(id ?: run {
            gradleProject.logger.warn("developer.id will set empty,because it is null")
            ""
        })
    }


}
