package tech.medivh.plugin.gradle.publisher


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
open class MedivhPublisherExtension {

    var dir: String = ""

    var hasJavaDoc = true

    var hasSources = true

    var tokenUsername: String? = null
    
    var tokenPassword: String? = null

    fun withoutJavaDocJar() {
        hasJavaDoc = false
    }

    fun withoutSourcesJar() {
        hasSources = false
    }
}
