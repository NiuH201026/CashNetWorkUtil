package com.cash.networkutil.smoketest

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 仅用于验证 core-network 基础链路(RetrofitFactory + OkHttp + 线程切换)是否工作正常。
 * 用的是公开测试接口 jsonplaceholder,返回结构不是 BaseResponse 包装格式,
 * 所以这里直接拿 List<PostBean>,不走 RxResponseTransformer。
 *
 * 验证通过后,这个文件和目录可以整个删掉,它不属于框架的一部分。
 */
interface SmokeTestApi {
    @GET("posts")
    fun getPosts(): Observable<List<PostBean>>
}

data class PostBean(
    val userId: Int? = null,
    val id: Int? = null,
    val title: String? = null,
    val body: String? = null
)