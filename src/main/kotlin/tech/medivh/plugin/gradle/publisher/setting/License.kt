package tech.medivh.plugin.gradle.publisher.setting

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.gradle.api.publish.maven.MavenPomLicense
import tech.medivh.plugin.gradle.publisher.gradleProject


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
class License : SettingMan<MavenPomLicense> {

    var name: String? = null

    var url: String? = null

    override fun setting(s: MavenPomLicense) {
        s.name.set(name ?: run {
            gradleProject.logger.warn("license.name will set empty,because it is null")
            ""
        })
        s.url.set(url ?: run {
            gradleProject.logger.warn("license.url will set empty,because it is null")
            ""
        })
    }

}
