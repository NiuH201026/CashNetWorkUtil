# ============================================================
# consumer-rules.pro
# 任何引入本 AAR 的宿主项目都会自动继承这里的规则,
# 不需要在宿主项目自己的 proguard-rules.pro 里重复配置。
# 目的:AAR 被混淆后不影响宿主项目已有混淆逻辑,只补充本模块必需的规则。
# ============================================================

# --- Gson ---
# 保留本框架内所有 data class 的字段名,避免混淆后字段名对不上导致解析失败
-keep class com.cash.core.network.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# --- Retrofit ---
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# --- RxJava2 ---
-dontwarn io.reactivex.**
-keep class io.reactivex.** { *; }

# 注意:这里只保留"本框架"相关的类,不会用 -keep class ** { *; }
# 这种大范围规则去污染宿主项目其他代码的混淆结果,避免影响宿主项目已有逻辑。