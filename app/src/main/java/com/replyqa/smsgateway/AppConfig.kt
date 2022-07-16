package com.replyqa.smsgateway

import android.content.Context

class AppConfig(
    var onesignal: String,
    var api: String,
    var country: String,
    var authorization: String
) {
    companion object {
        fun retrieve(c: Context): AppConfig {
            val dcountry = c.getString(R.string.default_country)
            val dapi = c.getString(R.string.default_api)
            val donesignal = c.getString(R.string.default_onesignal)
            val dauth = c.getString(R.string.default_auth)

            val sharedPref = c.getSharedPreferences("app_config", Context.MODE_PRIVATE)
            val country = sharedPref.getString("country", dcountry) ?: dcountry
            val api = sharedPref.getString("api", dapi) ?: dapi
            val onesignal = sharedPref.getString("onesignal", donesignal) ?: donesignal
            val auth = sharedPref.getString("auth", c.getString(R.string.default_auth)) ?: dauth
            return AppConfig(onesignal, api, country, auth)
        }

        fun save(config: AppConfig, c: Context) {
            val sharedPref = c.getSharedPreferences("app_config", Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString("country", config.country)
                putString("api", config.api)
                putString("onesignal", config.onesignal)
                putString("auth", config.authorization)
                apply()
            }
        }
    }
}