package com.cash.core.network

/**
 * 统一异常类型
 *
 * 三种来源:
 * 1. 网络层异常(超时/断网/DNS失败等) -> NetworkError
 * 2. 业务code非0 -> BusinessError,携带原始code方便上层按code做特殊分支处理(比如code==401跳登录)
 * 3. 数据解析异常(Gson转换失败、data为null但业务期望非null) -> ParseError
 *
 * ViewModel/Repository 层统一捕获 ApiException 即可,
 * 不需要再区分 IOException/JsonSyntaxException 等底层类型。
 */
sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** 网络层错误:断网、超时、DNS解析失败等 */
    class NetworkError(message: String = "网络异常,请检查网络连接", cause: Throwable? = null) :
        ApiException(message, cause)

    /** 业务错误:接口返回 code != 成功码。code 是 String 类型,匹配真实后端返回格式 */
    class BusinessError(val code: String, message: String) : ApiException(message)

    /** 解析错误:Gson转换失败,或成功响应但 data 为 null 而业务期望必须有值 */
    class ParseError(message: String = "数据解析失败", cause: Throwable? = null) :
        ApiException(message, cause)

    /** 未知错误兜底 */
    class UnknownError(message: String = "未知错误", cause: Throwable? = null) :
        ApiException(message, cause)

    companion object {
        /**
         * 将底层 Throwable 统一转换为 ApiException。
         * 在 RxJava 的 onErrorResumeNext / doOnError 里调用,保证抛给上层的永远是 ApiException。
         */
        fun from(throwable: Throwable): ApiException {
            return when (throwable) {
                is ApiException -> throwable
                is java.net.SocketTimeoutException,
                is java.net.UnknownHostException,
                is java.io.IOException -> NetworkError(cause = throwable)
                is com.google.gson.JsonSyntaxException,
                is com.google.gson.JsonParseException -> ParseError(cause = throwable)
                else -> UnknownError(cause = throwable)
            }
        }
    }
}