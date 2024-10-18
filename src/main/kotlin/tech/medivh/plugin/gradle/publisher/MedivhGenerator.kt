package tech.medivh.plugin.gradle.publisher

import java.io.File
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPom
import tech.medivh.plugin.gradle.publisher.setting.Developer


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
                groupId = extension.groupId ?: userMaven?.groupId
                artifactId = extension.artifactId ?: userMaven?.artifactId
                version = extension.version ?: userMaven?.version
                extension.pom?.run { execute(pom) }
            }
            checkAndFill(mavenPublication, userMaven?.pom)
        }
    }

    private fun checkAndFill(mavenPublication: MavenPublication, userMaven: MavenPom?) {
        val mavenPom = mavenPublication.pom
        check(mavenPom is DefaultMavenPom) { "userMaven is not DefaultMavenPom" }
        fillLicense(mavenPom, userMaven)
    }


    private fun fillLicense(pom: DefaultMavenPom, userPom: MavenPom?) {
        if (!pom.licenses.isNullOrEmpty()) {
            return
        }
        if (userPom == null || (userPom as DefaultMavenPom).licenses.isNullOrEmpty()) {
            pom.licenses { licenses ->
                licenses.license {
                    it.name.set("")
                    it.url.set("")
                }
            }
            return
        }
        userPom.licenses.forEach { userLicense ->
            pom.licenses { licenses ->
                licenses.license {
                    it.name.set(userLicense.name)
                    it.url.set(userLicense.url)
                }
            }
        }
    }


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
