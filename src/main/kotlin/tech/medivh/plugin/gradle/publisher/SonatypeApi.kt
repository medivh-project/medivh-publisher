package tech.medivh.plugin.gradle.publisher

import java.io.File
import java.io.IOException
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.EMPTY_REQUEST


/**
 *
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
object SonatypeApi {

    lateinit var authToken: String

    //  url see https://central.sonatype.org/publish/publish-portal-api/

    private const val UPLOAD_URL = "https://central.sonatype.com/api/v1/publisher/upload"

    private const val STATUS_URL = "https://central.sonatype.com/api/v1/publisher/status"

    fun upload(file: File, name: String = file.name): String {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "bundle",
                name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            ).build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .sonatypeAuth()
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return response.body?.string() ?: ""
            } else {
                throw IOException("Upload failed: ${response.code}")
            }
        }
    }


    fun verifyStatus(id: String) {

        val url = STATUS_URL.toHttpUrl().newBuilder().run {
            addQueryParameter("id", id)
            build()
        }

        val request = Request.Builder()
            .url(url)
            .sonatypeAuth()
            .post(EMPTY_REQUEST)
            .build()

        val response = OkHttpClient().newCall(request).execute()
        if (response.isSuccessful) {
            println("status successful: ${response.body?.string()}")
        } else {
            println("status failed: ${response.code} ${response.message}")
        }
    }

    private fun Request.Builder.sonatypeAuth(): Request.Builder {
        return header("Authorization", "Bearer $authToken")
    }

}
