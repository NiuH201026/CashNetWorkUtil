package com.cash.networkutil.smoketest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cash.core.base.UiState

/**
 * core-base 冒烟测试页面。
 *
 * 验证清单:
 * 1. 点"发起请求"看到 Loading -> Success 状态正常流转
 * 2. 快速连点"发起请求(忽略重复)"按钮好几下,观察 Logcat 里 OkHttp 请求日志,
 *    应该只打一次(其余几次点击被 IGNORE_IF_LOADING 策略吞掉,不会真正发请求)
 * 3. 快速连点"发起请求(取消上一次)"按钮好几下,观察 Logcat,
 *    应该每次点击都发了请求,但界面最终只展示最后一次的结果
 * 4. 点完请求后,趁请求还没返回(比如断网状态下点)迅速退出页面(按返回键关掉Activity),
 *    观察 Logcat 有没有打出 "onCleared 被调用",且退出之后不应该再有任何状态更新或崩溃
 */
@Composable
fun SmokeTestScreen() {
    val viewModel: SmokeTestViewModel = viewModel()
    val state by viewModel.postsState.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val text = when (val s = state) {
            is UiState.Loading -> "加载中..."
            is UiState.Success -> "成功,共${s.data.size}条,第一条标题:${s.data.firstOrNull()?.title}"
            is UiState.Error -> "失败:${s.exception.message}"
            null -> "尚未请求"
        }
        Text(text = text)
        Text(text = "实际发出请求次数:${viewModel.actualRequestCount}")

        Button(onClick = { viewModel.loadPostsIgnoreIfLoading() }) {
            Text("发起请求(忽略重复)")
        }
        Button(onClick = { viewModel.loadPostsCancelPrevious() }) {
            Text("发起请求(取消上一次)")
        }
    }
}