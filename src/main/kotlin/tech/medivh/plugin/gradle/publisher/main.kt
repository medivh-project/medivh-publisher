package tech.medivh.plugin.gradle.publisher

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*

fun main() = runBlocking {
    // 创建 HTTP 客户端，使用 CIO 引擎
    val client = HttpClient(CIO)

    try {
        // 发送 GET 请求
        val response: HttpResponse = client.get("https://httpbin.org/get")
        println(response.status)           // 输出响应状态码
        println(response.bodyAsText())     // 输出响应体文本
    } finally {
        client.close()  // 确保客户端关闭以释放资源
    }
}
