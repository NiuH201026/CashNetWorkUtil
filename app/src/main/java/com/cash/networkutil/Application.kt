package com.cash.networkutil

import android.app.Application
import com.cash.core.network.RetrofitFactory

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        RetrofitFactory.enableLog = true
//        RetrofitFactory.commonHeaderProvider = {
//            mapOf("token" to "123")
//        }
        RetrofitFactory.commonParamsProvider = {
            mapOf(
                "deviceId" to "1",
                "appVersion" to 2,
                "platform" to "android"
            )
        }
    }
}