package tech.medivh.plugin.gradle.publisher

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloperSpec
import org.gradle.api.publish.maven.MavenPomLicenseSpec
import org.gradle.api.publish.maven.MavenPomScm
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.MavenPomInternal
import tech.medivh.plugin.gradle.publisher.setting.Developer
import tech.medivh.plugin.gradle.publisher.setting.Pom
import tech.medivh.plugin.gradle.publisher.setting.Scm


/**
 *
 * generate or fill info.
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class MedivhGenerator(private val project: Project) {

    private val extension = project.extensions.getByType(MedivhPublisherExtension::class.java)

    private val git: Git by lazy {
        val gitDir = project.rootDir.resolve(".git")
        check(gitDir.exists() && gitDir.isDirectory) {
            throw IllegalStateException("can't detect developer info, please specify")
        }
        Git.open(gitDir)
    }

    fun generateMedivhMavenPublication() {
        val publications = project.extensions.getByType(PublishingExtension::class.java).publications
        val userMaven = project.userMavenPublication
        publications.create(extension.publicationName, MavenPublication::class.java) { mavenPublication ->
            mavenPublication.apply {
                from(project.components.getByName("java"))
                groupId = userMaven?.groupId ?: extension.groupId
                artifactId = userMaven?.artifactId ?: extension.artifactId
                version = userMaven?.version ?: extension.version
                pom {
                    aggregatePom(it)
                }
            }
            println("generate medivh maven publication")
        }
    }

    private fun aggregatePom(mavenPom: MavenPom) {
        println(mavenPom::class.java)
        println("----------------------")
        println(mavenPom is MavenPomInternal)
        val mapper = XmlMapper()
        val userPom = project.userMavenPublication?.pomXml()?.let {
            println(it.path)
            if (!it.exists()) {
                return@let null
            }
            mapper.readValue<Pom>(it)
        }
        println(project.medivhTempPublication.pomXml().path)
        val medivhTempPom = mapper.readValue<Pom>(project.medivhTempPublication.pomXml())
        mavenPom.licenses {
            fillLicense(it, userPom, medivhTempPom)
        }
        mavenPom.developers {
            fillDeveloper(it, userPom, medivhTempPom)
        }
        mavenPom.scm {
            fillScm(it, userPom, medivhTempPom)
        }
    }

    private fun fillLicense(licenses: MavenPomLicenseSpec, userPom: Pom?, medivhPom: Pom) {
        medivhPom.licenses?.run {
            forEach { license ->
                licenses.license {
                    it.name.set(license.name)
                    it.url.set(license.url)
                }
            }
            return
        }
        userPom?.licenses?.run {
            forEach { license ->
                licenses.license {
                    it.name.set(license.name)
                    it.url.set(license.url)
                }
            }
            return
        }
        licenses.license {
            it.name.set("")
            it.url.set("")
        }
    }

    private fun fillDeveloper(developers: MavenPomDeveloperSpec, userPom: Pom?, medivhTempPom: Pom) {
        userPom?.developers?.run {
            forEach { developer ->
                developers.developer {
                    it.id.set(developer.id)
                    it.name.set(developer.name)
                    it.email.set(developer.email)
                }
            }
            return
        }
        medivhTempPom.developers?.run {
            forEach { developer ->
                developers.developer {
                    it.id.set(developer.id)
                    it.name.set(developer.name)
                    it.email.set(developer.email)
                }
            }
            return
        }
        val developer = detectDeveloper()
        developers.developer {
            it.id.set(developer.id)
            it.name.set(developer.name)
            it.email.set(developer.email)
        }
    }

    private fun fillScm(scm: MavenPomScm, userPom: Pom?, medivhTempPom: Pom) {
        userPom?.scm?.run {
            this.setting(scm)
            return
        }
        medivhTempPom.scm?.run {
            this.setting(scm)
            return
        }
        val remoteUrl = detectRemoteUrl()
        val scm = Scm()
        scm.connection = "scm:git:$remoteUrl"
        scm.url = remoteUrl
    }


    //    private fun generatePom(pom: MavenPom, publication: MavenPublication) {
    //        pom.licenses { licenses ->
    //            //  todo detect license
    //            licenses.license {
    //                it.name.set("GPL-3.0 license")
    //                it.url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
    //            }
    //        }
    //        pom.developers {
    //            it.developer { setDev ->
    //                detectDeveloper().setting(setDev)
    //            }
    //        }
    //        val remoteUrl = detectRemoteUrl()
    //        pom.scm {
    //            Scm(remoteUrl).setting(it)
    //        }
    //        pom.name.set("${publication.groupId}:${publication.artifactId}")
    //        pom.description.set("a project for ${publication.artifactId}")
    //        pom.url.set(remoteUrl)
    //    }


    private fun detectRemoteUrl(): String? {
        val config = git.repository.config
        val remoteUrl = config.getSubsections("remote").find { config.getString("remote", it, "url") != null }
        if (remoteUrl != null) {
            val warnMessage = "can't detect remoteUrl info, please specify remote url"
            println("[WARN] $warnMessage")
            project.logger.warn(warnMessage)
        }
        return remoteUrl
    }

    private fun detectDeveloper(): Developer {
        git.repository.config.apply {
            val name = getString("user", null, "name")
            val email = getString("user", null, "email")
            if (name != null || email != null) {
                val warnMessage = "can't detect developer info, please specify developer info"
                println("[WARN] $warnMessage")
                project.logger.warn(warnMessage)
            }
            return Developer().apply {
                this.id = name
                this.name = name
                this.email = email
            }
        }
    }

    private fun MavenPublication.pomXml(): File {
        val publicationsDir = project.layout.buildDirectory.dir("publications").get().asFile
        return publicationsDir.resolve("${this.name}/").resolve("pom-default.xml")
    }


}
