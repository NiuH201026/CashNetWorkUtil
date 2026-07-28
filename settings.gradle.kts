pluginManagement {
    repositories {
        // 阿里云镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            name = "AliyunPublic"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            name = "AliyunGoogle"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
            name = "AliyunGradlePlugin"
        }
        // 华为云镜像（备用）
        maven {
            url = uri("https://repo.huaweicloud.com/repository/maven")
            name = "HuaweiCloud"
        }
        // 腾讯云镜像（备用）
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
            name = "TencentCloud"
        }
        // 官方源（最终 fallback）
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            name = "AliyunPublic"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            name = "AliyunGoogle"
        }
        // 华为云镜像（备用）
        maven {
            url = uri("https://repo.huaweicloud.com/repository/maven")
            name = "HuaweiCloud"
        }
        // 腾讯云镜像（备用）
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
            name = "TencentCloud"
        }
        // 官方源（最终 fallback）
        google()
        mavenCentral()
    }
}

rootProject.name = "NetWorkUtil"
include(":app")
include(":core-network")
include(":core-base")
