package tech.medivh.plugin.gradle.publisher

import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import tech.medivh.plugin.gradle.publisher.setting.Developer
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

    fun generateMavenPublication(publication: MavenPublication) {
        publication.apply {
            from(project.components.getByName("java"))
            groupId = extension.groupId
            artifactId = extension.artifactId
            version = extension.version
            pom { pom ->
                generatePom(pom, this)
            }
        }
    }

    private fun generatePom(pom: MavenPom, publication: MavenPublication) {
        pom.licenses { licenses ->
            //  todo detect license
            licenses.license {
                it.name.set("GPL-3.0 license")
                it.url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
            }
        }
        pom.developers {
            it.developer { setDev ->
                detectDeveloper().setting(setDev)
            }
        }
        val remoteUrl = detectRemoteUrl()
        pom.scm {
            Scm(remoteUrl).setting(it)
        }
        pom.name.set("${publication.groupId}:${publication.artifactId}")
        pom.description.set("a project for ${publication.artifactId}")
        pom.url.set(remoteUrl)
    }


    private fun detectRemoteUrl(): String {
        val config = git.repository.config
        val remoteUrl = config.getSubsections("remote").find { config.getString("remote", it, "url") != null }
        check(remoteUrl != null) {
            "can't detect remoteUrl info, please specify remote url"
        }
        return remoteUrl
    }

    private fun detectDeveloper(): Developer {
        git.repository.config.apply {
            val name = getString("user", null, "name")
            val email = getString("user", null, "email")
            check(name != null || email != null) {
                "can't detect developer info, please specify"
            }
            return Developer(name, email)
        }
    }

    fun generateMedivhMavenPublication() {
        val publications = project.extensions.getByType(PublishingExtension::class.java).publications

        val userMaven = publications.withType(MavenPublication::class.java).firstOrNull()

        publications.create(extension.publicationName, MavenPublication::class.java) { mavenPublication ->
            extension.pom?.run {
                execute(mavenPublication.pom)
            }
            mergePom(mavenPublication.pom, userMaven?.pom)
            mavenPublication.apply {
                from(project.components.getByName("java"))
                groupId = userMaven?.groupId ?: extension.groupId
                artifactId = userMaven?.artifactId ?: extension.artifactId
                version = userMaven?.version ?: extension.version
            }

        }
    }

    private fun mergePom(medivhPom: MavenPom, userPom: MavenPom?) {

        medivhPom.licenses {
            it.license {
            }
        }
        medivhPom.developers {
            
        }
        medivhPom.developers {
            it.developer { setDev ->
                detectDeveloper().setting(setDev)
            }
        }

        userPom?.name?.run {
            medivhPom.name.set(this)
        }
    }

}
