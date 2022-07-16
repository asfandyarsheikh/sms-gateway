package com.replyqa.smsgateway

import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.GsonBuilder
import com.jakewharton.processphoenix.ProcessPhoenix


class MainActivity : AppCompatActivity() {

    private lateinit var onesignal: TextInputEditText
    private lateinit var country: TextInputEditText
    private lateinit var api: TextInputEditText
    private lateinit var authorization: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonView = findViewById<TextView>(R.id.json_schema)
        val submit = findViewById<Button>(R.id.save)
        onesignal = findViewById(R.id.onesignal)
        country = findViewById(R.id.country)
        api = findViewById(R.id.rest)
        authorization = findViewById(R.id.authorization)

        retrieve(AppConfig.retrieve(this))

        submit.setOnClickListener { save() }

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        val json = SmsData("Pakistan Zindabad!", "+923330978601")

        val jsonOutput = gson.toJson(json)

        jsonView.text = jsonOutput

        smsPerms()
    }

    fun smsPerms() {
        if (ActivityCompat.checkSelfPermission(
                this,
                SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(SEND_SMS, RECEIVE_SMS),
                    10
                )
            }
        }
    }

    fun save() {
        val c = country.text.toString()
        val a = api.text.toString()
        val o = onesignal.text.toString()
        val auth = authorization.text.toString()
        AppConfig.save(AppConfig(o, a, c, auth), this)
        ProcessPhoenix.triggerRebirth(this);
    }

    private fun retrieve(config: AppConfig) {
        country.setText(config.country)
        api.setText(config.api)
        onesignal.setText(config.onesignal)
        authorization.setText(config.authorization)
    }

}