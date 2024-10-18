package tech.medivh.plugin.gradle.publisher.setting

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
class Pom {

    var developers: List<Developer>? = null

    var licenses: List<License>? = null

    var scm: Scm? = null

}
