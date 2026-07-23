package com.cash.networkutil.smoketest.realapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cash.core.base.BaseViewModel
import com.cash.core.base.UiState

/**
 * 注意构造参数类型是接口 ProduceRepository,不是具体的 ProduceRepositoryImpl。
 * 默认值给了具体实现,正常调用 ProduceViewModel() 和之前用法完全一样;
 * 但如果以后要写单元测试,可以这样构造:
 *   ProduceViewModel(repository = FakeProduceRepository())  // 自己写一个假实现,不发真实网络请求
 * 不需要为了测试把 core-network 真实跑起来。
 */
class ProduceViewModel(
    private val repository: ProduceRepository = ProduceRepositoryImpl()
) : BaseViewModel() {

    private val _rollImageState = MutableLiveData<UiState<List<RollImageBean>>>()
    val rollImageState: LiveData<UiState<List<RollImageBean>>> = _rollImageState

    fun loadMainRollImages(id: Int) {
        launchRequest(_rollImageState) {
            repository.getMainRollImages(id)
        }
    }
}