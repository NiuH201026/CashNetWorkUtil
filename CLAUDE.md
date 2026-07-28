# CashNetWorkUtil

基于 Retrofit + RxJava2 的 Android 网络请求库。

## 模块结构

```
core-network（数据层）← Retrofit 工厂、Gson 解析、Rx 线程切换、BaseRepository
       ↑
  core-base（表现层）  ← BaseViewModel、UiState、LiveData
       ↑
     app（Demo）       ← 冒烟测试、Compose UI
```

- **core-network** 和 **core-base** 各自独立发布到 JitPack（`com.github.NiuH201026.CashNetWorkUtil`）
- `core-network` 不依赖 `core-base`，保证非 UI 场景（Service、Worker）也能单独引用

## 关键设计决策

### BaseRepository 合并到 core-network（v1.0.1）

原来 `core-repository` 单独一个模块只放了一个 85 行的 `BaseRepository.kt`，其所有依赖（RetrofitFactory、RxResponseTransformer、IApiResponse）都在 `core-network` 中，独立模块只有维护成本没有隔离收益。**后续 BaseRepository 需要引入新依赖（如 Room 缓存）时再拆分。**

### Gson 解析是自动的

`RetrofitFactory` 注册了 `GsonConverterFactory.create()`，Retrofit 根据接口返回类型的泛型信息自动完成 `fromJson()`。业务层拿到的是已经反序列化的对象，不需要手动调 Gson。

### 响应解包链路

```
API 接口返回 Observable<BaseResponse<T>>
  → BaseRepository.unwrap() 调 RxResponseTransformer.handle()
    → 线程切换（io → main）+ 拆 BaseResponse 壳取 data
      → ViewModel 拿到 Observable<T>
```

### ViewModel 防重复请求

`BaseViewModel.launchRequest()` 通过 `ongoingRequests` Map 实现：
- `IGNORE_IF_LOADING`：上次请求未返回，新请求直接忽略（适合提交类）
- `CANCEL_PREVIOUS`：取消上次请求，以最新为准（适合列表刷新）

## 发布流程

1. 改 `core-network/build.gradle.kts` 和 `core-base/build.gradle.kts` 里的 `version`
2. 提交 + 打 tag（带 v 前缀）：`git tag vX.Y.Z`
3. 推送 tag：`git push origin vX.Y.Z`
4. JitPack 自动构建发布：`https://jitpack.io/#NiuH201026/CashNetWorkUtil`

## 技术栈

- Kotlin 2.0.21 / AGP 8.12.0
- Retrofit 2.11.0 + OkHttp 4.12.0 + RxJava 2.x + RxAndroid + Gson
- minSdk 24 / compileSdk 35 / JVM 17
