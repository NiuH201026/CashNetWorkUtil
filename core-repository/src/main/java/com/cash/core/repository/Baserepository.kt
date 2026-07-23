package com.cash.core.repository

import com.cash.core.network.IApiResponse
import com.cash.core.network.RetrofitFactory
import com.cash.core.network.RxResponseTransformer
import io.reactivex.Observable

/**
 * 所有业务 Repository 的基类。
 *
 * 职责很单一:统一"怎么创建 Retrofit 接口实例"+"怎么解包响应"这两件事,
 * 业务 Repository 只需要关心"调哪个接口、传什么参数"。
 *
 * unwrap()/unwrapNullable() 面向 IApiResponse 接口泛型工作,不绑定具体响应类,
 * 所以不同后端格式的项目都可以用同一套 BaseRepository,不需要为每个后端格式
 * 单独维护一个 Repository 基类。
 *
 * 用法(以框架内置的 BaseResponse 为例,如果后端格式不同,
 * 换成自己项目里实现了 IApiResponse 的响应类即可,其余代码不用变):
 *
 *   interface OrderApi {
 *       @GET("order/list")
 *       fun getOrderList(@Query("page") page: Int): Observable<BaseResponse<List<OrderBean>>>
 *   }
 *
 *   class OrderRepository : BaseRepository() {
 *       private val api = createApi(AppUrls.baseUrl, OrderApi::class.java)
 *
 *       fun getOrderList(page: Int): Observable<List<OrderBean>> {
 *           return api.getOrderList(page).unwrap()
 *       }
 *   }
 *
 * ViewModel 层拿到的 getOrderList(page) 已经是解包、切换好线程的 Observable<T>,
 * 直接传给 BaseViewModel.launchRequest 即可。
 *
 * ============================================================
 * 推荐写法:给每个 Repository 定义一个接口,ViewModel 依赖接口而不是具体实现类
 * ============================================================
 *
 * 上面的例子里 ViewModel 如果直接 new OrderRepository(),测试时就没法mock掉网络请求。
 * 推荐额外加一层接口抽象,成本很低(多一个interface文件),换来的好处是
 * 以后写单元测试可以直接mock接口,不需要为了测试把真实网络层跑起来:
 *
 *   interface OrderRepository {
 *       fun getOrderList(page: Int): Observable<List<OrderBean>>
 *   }
 *
 *   class OrderRepositoryImpl : BaseRepository(), OrderRepository {
 *       private val api = createApi(AppUrls.baseUrl, OrderApi::class.java)
 *       override fun getOrderList(page: Int) = api.getOrderList(page).unwrap()
 *   }
 *
 *   class OrderViewModel(
 *       private val repository: OrderRepository = OrderRepositoryImpl()  // 依赖接口,默认值给具体实现,调用方不用关心怎么构造
 *   ) : BaseViewModel() { ... }
 *
 * 这不是必须的——页面简单、不写单元测试的场景,直接用具体类也完全可以,
 * 不需要为了"看起来更规范"而强行加这层。按需引入。
 */
abstract class BaseRepository {

    /**
     * 创建一个 Retrofit 接口实例。baseUrl 由业务方决定,
     * 通常来自项目自己维护的 AppUrls 之类的单一数据源(测试/生产环境切换)。
     */
    protected fun <T> createApi(baseUrl: String, service: Class<T>): T {
        return RetrofitFactory.create(baseUrl, service)
    }

    /**
     * 解包响应,同时完成线程切换(io -> main)。
     * data 为 null 时会抛 ApiException.ParseError,适合"必须要有数据"的接口。
     */
    protected fun <T, R : IApiResponse<T>> Observable<R>.unwrap(): Observable<T> {
        return compose(RxResponseTransformer.handle())
    }

    /**
     * 允许 data 为 null 的场景(比如提交类接口只关心 code,不关心返回体)。
     */
    protected fun <T, R : IApiResponse<T>> Observable<R>.unwrapNullable(): Observable<T?> {
        return compose(RxResponseTransformer.handleNullable())
    }
}