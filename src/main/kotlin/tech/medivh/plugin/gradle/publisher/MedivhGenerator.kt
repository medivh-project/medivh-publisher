package tech.medivh.plugin.gradle.publisher

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import tech.medivh.plugin.gradle.publisher.setting.Developer
import tech.medivh.plugin.gradle.publisher.setting.Scm


/**
 *
 * generate or fill info.
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
class MedivhGenerator(private val project: Project) {

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
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            pom { pom ->
                generatePom(pom, this)
            }
        }
    }

    private fun generatePom(pom: MavenPom, publication: MavenPublication) {
        pom.name.set("${publication.groupId}:${publication.artifactId}")
        pom.description.set("a project for ${publication.artifactId}")
        pom.url.set("https://github.com")
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
        pom.scm {
            detectScm().setting(it)
        }
    }


    private fun detectScm(): Scm {
        val remoteUrl = git.repository.config.getString("remote", "origin", "url")
        check(remoteUrl != null) {
            "can't detect scm info, please specify"
        }
        return Scm(remoteUrl)
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

}
