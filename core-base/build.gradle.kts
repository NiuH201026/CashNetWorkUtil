plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    id("maven-publish")
}

android {
    namespace = "com.cash.core.base"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
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
                artifactId = "core-base"   // core-repository/core-base模块这里改成对应名字
                version = "1.0.0"
                from(components["release"])
            }
        }
    }
}

dependencies {
    // core-network 里的 ApiException、Observable<T> 会出现在本模块 public API 里,用 api 暴露
    api(project(":core-network"))

    // ViewModel/LiveData 这些类型同样暴露在 public API 里(BaseViewModel 继承 ViewModel,
    // UiState 配合 LiveData 使用),所以也用 api
    api(libs.lifecycle.viewmodel.ktx)
    api(libs.lifecycle.livedata.ktx)
}