package com.replyqa.smsgateway

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal.OSRemoteNotificationReceivedHandler


class NotificationServiceExtension : OSRemoteNotificationReceivedHandler {
    override fun remoteNotificationReceived(
        context: Context,
        notificationReceivedEvent: OSNotificationReceivedEvent
    ) {
        val notification = notificationReceivedEvent.notification
        sendSMS(AppConfig.retrieve(context), notification.title, notification.body)
        notificationReceivedEvent.complete(null)
    }

    fun sendSMS(config: AppConfig, phoneNo: String?, msg: String?) {
        if (!config.enabled) {
            return
        }
        if(phoneNo?.indexOf(config.country) != 0) {
            return
        }

        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(msg)
            smsManager.sendMultipartTextMessage(phoneNo, null, parts, null, null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}