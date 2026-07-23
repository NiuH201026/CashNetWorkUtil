# ============================================================
# consumer-rules.pro (core-base)
# 任何引入本 AAR 的宿主项目都会自动继承这里的规则。
# ============================================================

-keep class com.cash.core.base.** { *; }
-keepattributes Signature
-keepattributes *Annotation*