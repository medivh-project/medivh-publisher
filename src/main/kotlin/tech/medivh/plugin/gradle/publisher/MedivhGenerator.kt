package tech.medivh.plugin.gradle.publisher

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

    private val git: Git? by lazy {
        val gitDir = project.rootDir.resolve(".git")
        if (gitDir.exists() && gitDir.isDirectory) {
            Git.open(gitDir)
        } else {
            null
        }
    }

    fun generateMedivhMavenPublication() {
        val publications = project.extensions.getByType(PublishingExtension::class.java).publications
        val userMaven = project.userMavenPublication
        publications.create(extension.publicationName, MavenPublication::class.java) { mavenPublication ->
            mavenPublication.apply {
                from(project.components.getByName("java"))
                groupId = extension.groupId ?: userMaven?.groupId
                if (groupId == null) {
                    groupId = project.group.toString()
                }
                artifactId = extension.artifactId ?: userMaven?.artifactId
                if (artifactId == null) {
                    artifactId = project.name
                }
                extension.finalUploadName = artifactId
                version = extension.version ?: userMaven?.version
                if (version == null) {
                    version = project.version.toString()
                }
                extension.pom?.run { execute(pom) }
            }
            checkAndFill(mavenPublication, userMaven?.pom)
        }
    }

    private fun checkAndFill(mavenPublication: MavenPublication, userMaven: MavenPom?) {
        val mavenPom = mavenPublication.pom
        check(mavenPom is DefaultMavenPom) { "userMaven is not DefaultMavenPom" }
        if (mavenPom.url.orNull == null) {
            mavenPom.url.set(userMaven?.url?.orNull ?: (detectRemoteUrl() ?: ""))
        }
        if (mavenPom.description.orNull == null) {
            mavenPom.description.set(userMaven?.description?.orNull ?: "")
        }
        fillLicense(mavenPom, userMaven)
        fillDeveloper(mavenPom, userMaven)
        fillScm(mavenPom, userMaven)
    }

    private fun fillScm(finalPom: DefaultMavenPom, userMaven: MavenPom?) {
        if (finalPom.scm != null) {
            return
        }
        if (userMaven != null && (userMaven as DefaultMavenPom).scm != null) {
            finalPom.scm { scmSpec ->
                scmSpec.connection.set(userMaven.scm!!.connection)
                scmSpec.developerConnection.set(userMaven.scm!!.developerConnection)
                scmSpec.url.set(userMaven.scm!!.url)
            }
            return
        }
        finalPom.scm { scmSpec ->
            val remoteUrl = detectRemoteUrl()
            if (remoteUrl == null) {
                val warnMessage = "can't detect git remoteUrl info, scm will be empty"
                println("[WARN] $warnMessage")
                project.logger.warn(warnMessage)
                scmSpec.connection.set("")
                scmSpec.url.set("")
            } else {
                scmSpec.connection.set("scm:git:$remoteUrl")
                scmSpec.url.set(remoteUrl)
            }

        }
    }

    private fun fillDeveloper(finalPom: DefaultMavenPom, userMaven: MavenPom?) {
        if (!finalPom.developers.isNullOrEmpty()) {
            return
        }
        if (userMaven != null && ((userMaven as DefaultMavenPom).developers.isNotEmpty())) {
            userMaven.developers.forEach { userDeveloper ->
                finalPom.developers { developerSpec ->
                    developerSpec.developer {
                        it.name.set(userDeveloper.name)
                        it.id.set(userDeveloper.id)
                        it.email.set(userDeveloper.email)
                    }
                }
            }
            return
        }
        finalPom.developers { developerSpec ->
            developerSpec.developer {
                detectDeveloper().setting(it)
            }
        }
    }


    private fun fillLicense(finalPom: DefaultMavenPom, userMaven: MavenPom?) {
        if (!finalPom.licenses.isNullOrEmpty()) {
            return
        }
        if (userMaven != null && ((userMaven as DefaultMavenPom).licenses.isNotEmpty())) {
            userMaven.licenses.forEach { userLicense ->
                finalPom.licenses { licenses ->
                    licenses.license {
                        it.name.set(userLicense.name)
                        it.url.set(userLicense.url)
                    }
                }
            }
            return
        }
        finalPom.licenses { licenses ->
            licenses.license {
                it.name.set("")
                it.url.set("")
            }
        }

    }


    private fun detectRemoteUrl(): String? {
        val config = git?.repository?.config ?: return null
        return config.getSubsections("remote").find { config.getString("remote", it, "url") != null }
    }

    private fun detectDeveloper(): Developer {
        val dev = Developer()

        git!!.repository?.config?.run {
            val name = getString("user", null, "name")
            val email = getString("user", null, "email")
            dev.id = name
            dev.name = name
            dev.email = email
        }
        return dev
    }


}

