package com.cash.networkutil.smoketest.realapi

import com.cash.core.network.BaseResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

data class RollImageBean(
    val id: Int? = null,
    val productId: Int? = null,
    val imgType: Int? = null,
    val displayOrder: Int? = null,
    val imgDesc: String? = null,
    val imgUrl: String? = null,
    val isMainImg: Int? = null,
    val createTime: String? = null,
    val modifyTime: String? = null
)

interface ProduceApi {
    @GET("kitchen/api/v4/produce/mainRollImages/{id}")
    fun getMainRollImages(@Path("id") id: Int): Observable<BaseResponse<List<RollImageBean>>>
}