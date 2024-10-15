package tech.medivh.plugin.gradle.publisher.api

import java.io.File
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.EMPTY_REQUEST
import tech.medivh.plugin.gradle.publisher.MedivhPublisherExtension


/**
 *
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
object SonatypeApi {

    private lateinit var authToken: String

    //  url see https://central.sonatype.org/publish/publish-portal-api/

    private const val UPLOAD_URL = "https://central.sonatype.com/api/v1/publisher/upload"

    private const val STATUS_URL = "https://central.sonatype.com/api/v1/publisher/status"

    private const val PUBLISH_URL = "https://central.sonatype.com/api/v1/publisher/deployment/"

    fun init(extension: MedivhPublisherExtension) {
        extension.fillAfterEvaluate()
        authToken = calcAuthToken(extension.sonatypeUsername, extension.sonatypePassword)
    }

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
            return UploadResponseHandler.handleResponse(response)
        }
    }

    fun publish(id: String) {
        val url = "$PUBLISH_URL$id".toHttpUrl()
        val request = Request.Builder()
            .url(url)
            .sonatypeAuth()
            .post(EMPTY_REQUEST)
            .build()

        OkHttpClient().newCall(request).execute().use { response ->
            return PublishResponseHandler.handleResponse(response)
        }
    }

    fun dropDeployment(id: String) {
        val url = "$PUBLISH_URL$id".toHttpUrl()
        val request = Request.Builder()
            .url(url)
            .sonatypeAuth()
            .delete()
            .build()

        OkHttpClient().newCall(request).execute().use { response ->
            DropResponseHandler.handleResponse(response)
        }
    }

    fun deploymentState(id: String): DeploymentState {
        val url = STATUS_URL.toHttpUrl().newBuilder().run {
            addQueryParameter("id", id)
            build()
        }

        val request = Request.Builder()
            .url(url)
            .sonatypeAuth()
            .post(EMPTY_REQUEST)
            .build()

        OkHttpClient().newCall(request).execute().use { response ->
            return StatusResponseHandler.handleResponse(response)
        }
    }

    private fun Request.Builder.sonatypeAuth(): Request.Builder {
        return header("Authorization", "Bearer $authToken")
    }

}
