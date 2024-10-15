package tech.medivh.plugin.gradle.publisher.api

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author gxz gongxuanzhangmelt@gmail.com
 **/


/**
 * see https://central.sonatype.org/publish/publish-portal-api/
 */
fun calcAuthToken(tokenUsername: String?, tokenPassword: String?): String {
    //  todo 
    tokenUsername ?: throw IllegalStateException("username is null https://central.sonatype.com/account")
    tokenPassword ?: throw IllegalStateException("password is null")
    return Base64.getEncoder().encodeToString("$tokenUsername:$tokenPassword".toByteArray())
}

fun zipFolder(folder: File, targetFile: File): File {
    require(folder.exists()) { "${folder.absolutePath} does not exist" }
    require(folder.isDirectory) { "${folder.absolutePath} is not a directory" }

    if (!targetFile.parentFile.exists()) {
        targetFile.parentFile.mkdirs()
    }
    if (targetFile.exists()) {
        targetFile.delete()
    }
    ZipOutputStream(BufferedOutputStream(FileOutputStream(targetFile))).use { zipOut ->
        folder.walkTopDown().forEach { file ->
            if (file != folder) {
                val relativePath = file.relativeTo(folder).path

                if (file.isDirectory) {
                    zipOut.putNextEntry(ZipEntry("$relativePath/"))
                    zipOut.closeEntry()
                } else {
                    zipOut.putNextEntry(ZipEntry(relativePath))
                    file.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
        }
    }
    return targetFile
}



