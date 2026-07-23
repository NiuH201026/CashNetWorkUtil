package com.cash.networkutil.smoketest.realapi

import com.cash.core.repository.BaseRepository
import io.reactivex.Observable

/**
 * 接口:定义"能做什么",不涉及Retrofit/网络细节。
 * ViewModel 只认识这个接口,不知道背后是真实网络请求还是测试用的假实现。
 */
interface ProduceRepository {
    fun getMainRollImages(id: Int): Observable<List<RollImageBean>>
}

/**
 * 实现类:真正持有 Retrofit 接口实例,干活的地方。
 * 类名加 Impl 后缀是约定俗成的命名方式,一眼能看出这是某个接口的实现。
 */
class ProduceRepositoryImpl : BaseRepository(), ProduceRepository {

    private val api = createApi("https://www.glaya.shop/", ProduceApi::class.java)

    override fun getMainRollImages(id: Int): Observable<List<RollImageBean>> {
        return api.getMainRollImages(id).unwrap()
    }
}