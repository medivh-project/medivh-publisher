package tech.medivh.plugin.gradle.publisher


/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class MedivhPublisherExtension {
    
    var dir: String = ""

    var hasJavaDoc = true

    var hasSources = true

    fun withoutJavaDocJar() {
        hasJavaDoc = false
    }

    fun withoutSourcesJar() {
        hasSources = false
    }
}
