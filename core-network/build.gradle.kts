plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.library)

    id("maven-publish")
}

android {
    namespace = "com.cash.core.network"
    compileSdk = 35

    defaultConfig {
        // 不要 applicationId —— Library没有这个概念,AAR被谁引用就用谁的applicationId
        // 不要 targetSdk —— defaultConfig里的targetSdk在Library里从AGP 7.0起就被废弃/忽略了
        // 不要 versionCode/versionName —— Library不产出独立版本的APK,这两个字段对AAR无意义
        // testInstrumentationRunner 如果要写单元测试可以保留,不影响

        consumerProguardFiles("consumer-rules.pro") // 现在可以正常解析了
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            // 如果以后有debug单独发布的需求可以加 withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.cash"
                artifactId = "core-network"   // core-repository/core-base模块这里改成对应名字
                version = "1.0.0"
                from(components["release"])
            }
        }
    }
}
dependencies {
    // ---- 以下几个必须用 api,因为类型会暴露在本模块 public 方法签名里 ----
    // (BaseResponse<T> 里用到 -> 无所谓,是本模块自己的类;
    //  但 Observable<T>、Retrofit 相关类型确实要暴露)
    api(libs.retrofit.core)
    api(libs.retrofit.rxjava2)
    api(libs.rxjava)
    api(libs.rxandroid)
    api(libs.gson)

    // ---- 以下纯内部实现细节,不暴露给消费方,用 implementation ----
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
}