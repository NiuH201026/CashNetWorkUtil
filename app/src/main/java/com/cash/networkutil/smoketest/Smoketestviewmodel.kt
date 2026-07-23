package com.cash.networkutil.smoketest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cash.core.base.BaseViewModel
import com.cash.core.network.RetrofitFactory
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * core-base 冒烟测试 ViewModel。
 * 验证通过后整个 smoketest 包可以删掉,不属于框架的一部分。
 */
class SmokeTestViewModel : BaseViewModel() {

    private val _postsState = MutableLiveData<com.cash.core.base.UiState<List<PostBean>>>()
    val postsState: LiveData<com.cash.core.base.UiState<List<PostBean>>> = _postsState

    private val api by lazy {
        RetrofitFactory.enableLog = true
        RetrofitFactory.create(
            "https://jsonplaceholder.typicode.com/",
            SmokeTestApi::class.java
        )
    }

    /** 记录实际发出去的请求次数,用于验证 IGNORE_IF_LOADING 策略是否生效 */
    var actualRequestCount = 0
        private set

    /**
     * 默认策略 IGNORE_IF_LOADING:快速点击多次,只应该真正发出一次请求。
     * jsonplaceholder 接口很快,想测出"忽略"效果建议在弱网/加了延迟的情况下测,
     * 或者看 actualRequestCount 是否被后续多次调用叠加(应该始终只有一次生效)。
     */
    fun loadPostsIgnoreIfLoading() {
        launchRequest(_postsState) {
            actualRequestCount++
            api.getPosts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * CANCEL_PREVIOUS 策略:快速连续调用,前一次应该被取消,
     * 最终只有最后一次的结果会更新到 UiState。
     */
    fun loadPostsCancelPrevious() {
        launchRequest(_postsState, strategy = RequestStrategy.CANCEL_PREVIOUS) {
            actualRequestCount++
            api.getPosts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 验证用:ViewModel 真正被清理时应该打印这行日志,
        // 配合 Logcat 过滤 "SmokeTestViewModel" 确认 onCleared 被触发、
        // 且触发之后不会再有 postsState 的更新(否则说明 Disposable 没清干净)。
        android.util.Log.d("SmokeTestViewModel", "onCleared 被调用,disposables 已清空")
    }
}