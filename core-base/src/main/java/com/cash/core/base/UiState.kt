package com.cash.core.base

import com.cash.core.network.ApiException

/**
 * 统一的 UI 状态封装。
 *
 * ViewModel 对外暴露 LiveData<UiState<T>>,View 层(不管是 XML 还是 Compose)
 * 只需要 observe 这一个 LiveData,自己 when 分支处理三种状态,
 * 不用再各自维护"loading要不要显示菊花"、"报错文案怎么取"这些重复逻辑。
 */
sealed class UiState<out T> {

    /** 请求发起中,View 层收到这个状态应该显示 loading */
    data object Loading : UiState<Nothing>()

    /** 请求成功,携带解包后的数据 */
    data class Success<T>(val data: T) : UiState<T>()

    /** 请求失败,携带统一异常类型,View 层可以按 ApiException 的子类型区分展示方式 */
    data class Error(val exception: ApiException) : UiState<Nothing>()
}

/**
 * 语法糖:只关心成功数据时用这个,忽略 Loading/Error。
 * 常见场景:列表下拉刷新只需要处理"刷新成功后更新数据"这一条分支。
 */
inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

inline fun <T> UiState<T>.onError(action: (ApiException) -> Unit): UiState<T> {
    if (this is UiState.Error) action(exception)
    return this
}

inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}