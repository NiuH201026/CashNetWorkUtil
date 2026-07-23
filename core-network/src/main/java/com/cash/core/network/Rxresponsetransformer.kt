package com.cash.core.network

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * 统一响应解包 + 调度器切换的 Transformer。
 *
 * 泛型约束是 R : IApiResponse<T>,不绑定任何具体响应类。
 * 只要某个项目的响应类实现了 IApiResponse 接口(不管字段名、code类型是什么),
 * 都可以直接用这个 Transformer,这也是这段逻辑能打进AAR、以后不用改的关键。
 *
 * 用法(Repository 层):
 *   api.getOrderList(params)
 *      .compose(RxResponseTransformer.handle())
 *      .subscribe(onNext, onError)
 *
 * 业务层 subscribe 拿到的直接是解包后的 T,不用每次手写 .map { it.responseData }。
 * data 为 null 时统一抛 ApiException.ParseError,不会让业务层拿到一个隐藏的 null。
 */
object RxResponseTransformer {

    fun <T, R : IApiResponse<T>> handle(): ObservableTransformer<R, T> {
        return ObservableTransformer { upstream ->
            upstream
                .subscribeOn(Schedulers.io())
                .flatMap { response -> unwrap(response) }
                .onErrorResumeNext { throwable: Throwable ->
                    Observable.error(ApiException.from(throwable))
                }
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 允许 data 为 null 的场景(比如提交类接口只关心 code,不关心返回体)。
     * 用这个变体,上层拿到的是 T?,自己决定怎么处理 null。
     */
    fun <T, R : IApiResponse<T>> handleNullable(): ObservableTransformer<R, T?> {
        return ObservableTransformer { upstream ->
            upstream
                .subscribeOn(Schedulers.io())
                .flatMap { response ->
                    if (response.isSuccess()) {
                        Observable.just(response.responseData)
                    } else {
                        Observable.error(
                            ApiException.BusinessError(response.code, response.message ?: "请求失败")
                        )
                    }
                }
                .onErrorResumeNext { throwable: Throwable ->
                    Observable.error(ApiException.from(throwable))
                }
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun <T, R : IApiResponse<T>> unwrap(response: R): ObservableSource<T> {
        if (!response.isSuccess()) {
            return Observable.error(
                ApiException.BusinessError(response.code, response.message ?: "请求失败")
            )
        }
        val data = response.responseData
            ?: return Observable.error(ApiException.ParseError("成功响应但 data 为空"))
        return Observable.just(data)
    }
}