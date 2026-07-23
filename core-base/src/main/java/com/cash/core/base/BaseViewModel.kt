package com.cash.core.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cash.core.network.ApiException
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * 所有业务 ViewModel 的基类。
 *
 * 核心解决的问题:老项目里 Disposable 经常是在 Activity/Fragment/Dialog 里
 * 手动 new CompositeDisposable(),自己记得在 onDestroy 里 clear(),
 * 一旦漏写就是内存泄漏或者页面销毁后回调还在跑导致崩溃
 * (对应 OrderAccountAddDialog 那次的教训)。
 *
 * 这里统一收进 ViewModel 生命周期管理,业务方完全不用感知 Disposable 的存在。
 */
abstract class BaseViewModel : ViewModel() {

    protected val disposables = CompositeDisposable()

    /** 记录每个 liveData 当前正在进行中的请求,用于防重复触发 */
    private val ongoingRequests = mutableMapOf<MutableLiveData<*>, Disposable>()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        ongoingRequests.clear()
    }

    /**
     * 请求去重策略:
     * - IGNORE_IF_LOADING:如果上一次请求还没返回,这次调用直接忽略。
     *   适合"提交订单""支付"这类按钮点击触发的请求,防止手速快点两下发出两次提交。
     * - CANCEL_PREVIOUS:取消上一次还没返回的请求,以这次为准。
     *   适合"下拉刷新""切换筛选条件重新查询"这类以最新一次操作结果为准的场景。
     */
    enum class RequestStrategy {
        IGNORE_IF_LOADING,
        CANCEL_PREVIOUS
    }

    /**
     * 发起一次请求并自动维护 UiState,默认按 IGNORE_IF_LOADING 策略防重复。
     *
     * 用法(Repository 层已经用 RxResponseTransformer.handle() 处理过线程切换和解包,
     * 这里拿到的 Observable<T> 直接是成功值,失败会走 onError):
     *
     *   fun submitOrder() {
     *       launchRequest(submitState) {
     *           repository.submitOrder(params)
     *       }
     *   }
     *
     *   // 列表刷新场景,想要"以最新一次筛选条件为准"就传 CANCEL_PREVIOUS:
     *   fun loadOrders(page: Int) {
     *       launchRequest(orderListState, strategy = RequestStrategy.CANCEL_PREVIOUS) {
     *           repository.getOrderList(page)
     *       }
     *   }
     *
     * View 层只需要 observe liveData,不用关心请求什么时候发起、什么时候结束。
     */
    protected fun <T> launchRequest(
        liveData: MutableLiveData<UiState<T>>,
        strategy: RequestStrategy = RequestStrategy.IGNORE_IF_LOADING,
        request: () -> Observable<T>
    ) {
        val ongoing = ongoingRequests[liveData]
        if (ongoing != null && !ongoing.isDisposed) {
            when (strategy) {
                RequestStrategy.IGNORE_IF_LOADING -> return
                RequestStrategy.CANCEL_PREVIOUS -> {
                    ongoing.dispose()
                    disposables.remove(ongoing)
                }
            }
        }

        liveData.value = UiState.Loading
        val disposable = request()
            .subscribe(
                { data ->
                    ongoingRequests.remove(liveData)
                    liveData.value = UiState.Success(data)
                },
                { throwable ->
                    ongoingRequests.remove(liveData)
                    liveData.value = UiState.Error(ApiException.from(throwable))
                }
            )
        ongoingRequests[liveData] = disposable
        disposables.add(disposable)
    }
}