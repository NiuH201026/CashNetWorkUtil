plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    id("maven-publish")
}

android {
    namespace = "com.cash.core.repository"
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
                artifactId = "core-repository"   // core-repository/core-base模块这里改成对应名字
                version = "1.0.0"
                from(components["release"])
            }
        }
    }
}
dependencies {
    // 只依赖 core-network,不依赖 core-base。
    // Repository 属于数据层,不应该感知 ViewModel/LiveData 这些表现层概念,
    // 这样以后如果有非 UI 场景(后台 Service、WorkManager 任务)要复用 Repository,
    // 不会被迫引入一堆和它无关的表现层依赖。
    api(project(":core-network"))
}