# ============================================================
# consumer-rules.pro (core-repository)
# 任何引入本 AAR 的宿主项目都会自动继承这里的规则。
# ============================================================

-keep class com.cash.core.repository.** { *; }
-keepattributes Signature
-keepattributes *Annotation*