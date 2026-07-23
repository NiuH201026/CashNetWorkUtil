package com.cash.core.network

/**
 * 统一响应契约接口。
 *
 * 不同后端项目的响应格式往往不一样(字段名不同、code是Int还是String、
 * 成功判定标准不同),这个接口不关心具体格式,只约定"任何响应类
 * 都必须能回答这几个问题":有没有成功、消息是什么、数据是什么、code是什么。
 *
 * 框架里的 RxResponseTransformer、BaseRepository 等逻辑全部面向这个接口工作,
 * 不依赖任何具体的响应类,所以这部分逻辑打进AAR之后永远不用改。
 *
 * 每个具体项目根据自己后端实际返回的JSON格式,写一个实现类
 * (通常就是一个 data class,几行代码),这部分代码留在各项目自己仓库里,
 * 不进AAR——以后换后端格式、接新项目,只改这几行,AAR完全不用动。
 *
 * 用法示例(某个后端返回 {"code":0,"msg":"...","data":{...}}):
 *
 *   data class CrmResponse<T>(
 *       override val code: String = "-1",
 *       val msg: String? = null,
 *       val data: T? = null
 *   ) : IApiResponse<T> {
 *       override fun isSuccess() = code == "0"
 *       override val message: String? get() = msg
 *       override val responseData: T? get() = data
 *   }
 *
 *   // 如果后端 code 本身是 Int,构造参数单独接收 Int,再转成 String 赋给 code:
 *   data class CrmResponse<T>(
 *       val rawCode: Int = -1,
 *       val msg: String? = null,
 *       val data: T? = null
 *   ) : IApiResponse<T> {
 *       override val code: String get() = rawCode.toString()
 *       override fun isSuccess() = rawCode == 0
 *       override val message: String? get() = msg
 *       override val responseData: T? get() = data
 *   }
 *
 * 两个项目的 API 接口分别声明各自的响应类型,都可以直接
 * .compose(RxResponseTransformer.handle()) 使用,框架逻辑完全通用。
 */
interface IApiResponse<T> {
    val code: String
    val message: String?
    val responseData: T?
    fun isSuccess(): Boolean
}