package com.cash.core.network

import com.google.gson.annotations.SerializedName

/**
 * 框架内置的默认响应实现,匹配 {"code":"0","message":"...","data":{...}} 这种格式
 * (实测对应 glaya.shop 的真实接口)。
 *
 * 这只是 IApiResponse 接口的其中一种实现,不是唯一选择。
 * 如果新接入的项目后端格式不一样(字段名不同、code是Int类型等),
 * 不要改这个类,而是在那个项目自己的代码里另写一个 IApiResponse 实现类
 * (参考 IApiResponse.kt 里的示例),两者可以在不同项目里并存使用,
 * 框架逻辑(RxResponseTransformer等)对两者都通用。
 *
 * data 字段必须声明为可空(T?):Gson 通过反射构造对象,会绕过 Kotlin 的
 * 非空校验,即使这里写成非空 T,服务端返回 null 时依然会被塞进去,
 * 后续访问时才会在业务层抛 NPE,难以定位。统一在这里声明可空,
 * 强制业务层在读取时做判空处理。
 *
 * code 直接用 override val 覆盖接口属性,因为JSON字段名恰好也叫"code",
 * 属性名和JSON key一致不需要额外 @SerializedName。
 * msg/data 因为JSON字段名和接口属性名不一致(message vs message字段名对上了,
 * 但接口属性叫 message、这里构造参数叫 msg 便于内部使用),用自定义getter桥接,
 * 避免和 data class 自动生成的方法签名冲突。
 */
data class BaseResponse<T>(
    override val code: String = "-1",
    @SerializedName("message")
    val msg: String? = null,
    val data: T? = null
) : IApiResponse<T> {
    override fun isSuccess(): Boolean = code == "0"
    override val message: String? get() = msg
    override val responseData: T? get() = data
}