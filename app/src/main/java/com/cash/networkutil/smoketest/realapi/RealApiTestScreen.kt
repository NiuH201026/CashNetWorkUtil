package com.cash.networkutil.smoketest.realapi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cash.core.base.UiState
import com.cash.core.network.ApiException
import com.cash.core.network.RetrofitFactory

/**
 * 真实接口冒烟测试页面,对接 https://www.glaya.shop/kitchen/api/v4/produce/mainRollImages/{id}
 *
 * 验证清单:
 * 1. 点按钮后界面应显示成功解包出来的第一张图片URL
 * 2. Logcat 里能看到完整请求日志和原始JSON响应
 * 3. 确认 BaseResponse 的 code(String类型)、message 字段解析正常,没有 JsonSyntaxException
 */
@Composable
fun RealApiTestScreen() {
    val viewModel: ProduceViewModel = viewModel()
    val state by viewModel.rollImageState.observeAsState()

    DisposableEffect(Unit) {
        RetrofitFactory.enableLog = true
        onDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val text = when (val s = state) {
            is UiState.Loading -> "加载中..."
            is UiState.Success -> "成功,共${s.data.size}条,第一条imgUrl:${s.data.firstOrNull()?.imgUrl}"
            is UiState.Error -> {
                val prefix = when (s.exception) {
                    is ApiException.BusinessError -> "[业务错误]"
                    is ApiException.ParseError -> "[解析错误]"
                    is ApiException.NetworkError -> "[网络错误]"
                    else -> "[未知错误]"
                }
                "$prefix ${s.exception.message}"
            }
            null -> "尚未请求"
        }
        Text(text = text)

        Button(onClick = { viewModel.loadMainRollImages(1) }) {
            Text("请求真实接口 id=1")
        }
    }
}