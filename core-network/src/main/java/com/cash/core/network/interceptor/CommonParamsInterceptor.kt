package com.cash.core.network.interceptor

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer

/**
 * 统一注入公共参数(deviceId、appVersion、platform等)。
 * GET/无body请求拼到query上;JSON body请求合并进JSON对象(不覆盖业务已传字段);
 * 表单/multipart等其他body类型原样透传,不处理。
 */
class CommonParamsInterceptor(
    private val paramsProvider: () -> Map<String, Any>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val commonParams = paramsProvider()
        if (commonParams.isEmpty()) return chain.proceed(original)

        val newRequest = when (original.method) {
            "GET" -> {
                // GET请求(OkHttp不允许GET带body,只能走query,这是协议层限制)
                val newUrl = original.url.newBuilder().apply {
                    commonParams.forEach { (k, v) -> addQueryParameter(k, v.toString()) }
                }.build()
                original.newBuilder().url(newUrl).build()
            }
            "POST", "PUT", "PATCH" -> {
                val originalBody = original.body
                // 已有JSON body:合并进去,业务字段优先(不覆盖已存在的key)
                val jsonObject = if (originalBody != null &&
                    originalBody.contentType()?.toString()?.contains("json") == true
                ) {
                    val buffer = Buffer()
                    originalBody.writeTo(buffer)
                    JsonParser.parseString(buffer.readUtf8()).asJsonObject
                } else {
                    // 没有body(无参POST)或非JSON body(表单/multipart暂不处理,原样透传见else分支):
                    // 构造一个只包含公共参数的新JSON对象
                    JsonObject()
                }
                commonParams.forEach { (k, v) ->
                    if (!jsonObject.has(k)) jsonObject.addProperty(k, v.toString())
                }
                val newBody = jsonObject.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                original.newBuilder().method(original.method, newBody).build()
            }
            else -> original // DELETE等其他method暂不处理,原样透传
        }

        return chain.proceed(newRequest)
    }
}