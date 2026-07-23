package com.cash.core.network

// 注意:build.gradle.kts 中 retrofit / rxjava2 / gson 必须用 api 暴露(不能是 implementation),
// 因为 BaseResponse<T>、Observable<T> 这些类型会出现在本模块对外的方法签名里。
// 如果用 implementation,任何宿主项目引入 AAR 后编译时都会报 "unresolved reference"。
import com.cash.core.network.interceptor.CommonParamsInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Retrofit 创建入口,单一数据源。
 *
 * 使用方按 baseUrl 取实例,同一个 baseUrl 复用同一个 Retrofit(内部做了缓存),
 * 避免每个 Repository 各自 new 一份 OkHttpClient 导致连接池不复用。
 *
 * host 切换(测试/生产环境)不在这里做判断,由外部传入最终 baseUrl,
 * 上层可以参考老项目 AppUrls.kt 的做法:定义一个枚举 + 单一数据源类
 * 决定当前环境对应的 baseUrl 字符串,再传进来。
 *
 * 添加header头以及同一参数配置方法
 *  RetrofitFactory.enableLog = true
 * //        RetrofitFactory.commonHeaderProvider = {
 * //            mapOf("token" to "123")
 * //        }
 *         RetrofitFactory.commonParamsProvider = {
 *             mapOf(
 *                 "deviceId" to "1",
 *                 "appVersion" to 2,
 *                 "platform" to "android"
 *             )
 *         }
 */
object RetrofitFactory {

    private val retrofitCache = ConcurrentHashMap<String, Retrofit>()

    /** 是否开启日志拦截器,Debug包打开,Release包务必关闭避免泄露接口信息 */
    var enableLog: Boolean = false

    /** 公共请求头,业务方可在 App 初始化时注入(如 token、设备信息) */
    var commonHeaderProvider: (() -> Map<String, String>)? = null

    /** 公共请求参数,业务方可在 App 初始化时注入(如 deviceId、appVersion、platform）；
     * GET拼到query,JSON body合并进body,具体逻辑见 CommonParamsInterceptor */
    var commonParamsProvider: (() -> Map<String, Any>)? = null

    fun <T> create(baseUrl: String, service: Class<T>): T {
        val retrofit = retrofitCache.getOrPut(baseUrl) { buildRetrofit(baseUrl) }
        return retrofit.create(service)
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                commonHeaderProvider?.invoke()?.forEach { (key, value) ->
                    builder.header(key, value)
                }
                chain.proceed(builder.build())
            }
            .apply {
                // 公共参数拦截器要放在header拦截器之后、日志拦截器之前,
                // 这样开启 enableLog 时能看到合并公共参数之后的最终请求,方便调试
                commonParamsProvider?.let { provider ->
                    addInterceptor(CommonParamsInterceptor(provider))
                }
            }
            .apply {
                if (enableLog) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()

        // baseUrl 末尾必须带 "/",否则 @POST("order/list") 这类相对路径会拼接错误,
        // 这是老项目里已经踩过的坑,这里直接兜底校正。
        val safeBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(safeBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    /** 测试/切换环境时清空缓存,让下次 create 重新构建 */
    fun clearCache() {
        retrofitCache.clear()
    }
}