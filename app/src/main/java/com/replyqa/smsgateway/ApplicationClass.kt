package com.replyqa.smsgateway

import android.app.Application
import com.onesignal.OneSignal

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()

        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        val config = AppConfig.retrieve(this)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(config.onesignal)
    }
}