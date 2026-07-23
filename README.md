# CashNetWorkUtil

基于 Retrofit + RxJava2 的 Android 网络请求库，提供三个分层模块。

## 模块

| 模块 | 说明 | 依赖 |
|---|---|---|
| `core-network` | 网络基础层：Retrofit 工厂、拦截器、响应封装、Rx 线程切换 | — |
| `core-base` | 表现层基础：BaseViewModel、UiState、LiveData | core-network |
| `core-repository` | 数据仓库层：BaseRepository 抽象 | core-network |

## 集成

### 1. 添加 JitPack 仓库

`settings.gradle.kts`：

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. 添加依赖

```kotlin
// 基础网络层（必选）
implementation("com.github.NiuH201026.CashNetWorkUtil:core-network:v1.0.0")

// 表现层基础（可选，需要 ViewModel/LiveData 时引入）
implementation("com.github.NiuH201026.CashNetWorkUtil:core-base:v1.0.0")

// 数据仓库层（可选，需要 Repository 抽象时引入）
implementation("com.github.NiuH201026.CashNetWorkUtil:core-repository:v1.0.0")
```

## 核心类

### core-network

```kotlin
// 构建 Retrofit 实例
val retrofit = RetrofitFactory.create(
    baseUrl = "https://api.example.com/",
    commonParamsProvider = { mapOf("token" to "...") }
)

// API 响应统一封装
interface Api : IApiResponse {
    @GET("data")
    fun getData(): Observable<BaseResponse<DataBean>>
}
```

### core-base

```kotlin
// ViewModel 基类，统一管理 Loading/Error/Success 状态
class MyViewModel : BaseViewModel() {
    val data: LiveData<UiState<List<Item>>> = ...
}
```

### core-repository

```kotlin
// Repository 基类
class MyRepository : BaseRepository() {
    fun fetchData(): Observable<DataBean> = ...
}
```

## 技术栈

- Kotlin 2.0.21
- AGP 8.12.0
- Retrofit 2.11.0 + OkHttp 4.12.0
- RxJava 2.x + RxAndroid
- Gson
- minSdk 24 / compileSdk 35