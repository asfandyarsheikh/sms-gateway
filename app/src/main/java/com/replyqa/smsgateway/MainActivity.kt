package com.replyqa.smsgateway

import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.BarcodeUtils.decodeBitmap
import com.budiyev.android.codescanner.BarcodeUtils.encodeBitmap
import com.codekidlabs.storagechooser.StorageChooser
import com.codekidlabs.storagechooser.StorageChooser.OnSelectListener
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.GsonBuilder
import com.google.zxing.BarcodeFormat
import com.jakewharton.processphoenix.ProcessPhoenix
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var enabled: SwitchMaterial
    private lateinit var onesignal: TextInputEditText
    private lateinit var country: TextInputEditText
    private lateinit var api: TextInputEditText
    private lateinit var authorization: TextInputEditText

    final var CAMERA = 8787
    final var WRITE = 7887
    final var READ = 8797

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonView = findViewById<TextView>(R.id.json_schema)
        val submit = findViewById<Button>(R.id.save)
        val uploadBtn = findViewById<Button>(R.id.upload)
        val downloadBtn = findViewById<Button>(R.id.download)
        val scanBtn = findViewById<Button>(R.id.scan)
        onesignal = findViewById(R.id.onesignal)
        country = findViewById(R.id.country)
        api = findViewById(R.id.rest)
        authorization = findViewById(R.id.authorization)
        enabled = findViewById<SwitchMaterial>(R.id.enabled)

        retrieve(AppConfig.retrieve(this))

        submit.setOnClickListener { save() }
        scanBtn.setOnClickListener { scan() }
        uploadBtn.setOnClickListener { upload() }
        downloadBtn.setOnClickListener { download() }

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

    fun download() {
        saveP()
        if (ActivityCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(WRITE_EXTERNAL_STORAGE),
                    WRITE
                )
            }
        } else {
            downloadP()
        }
    }

    fun downloadP() {
        val downloadChooser = StorageChooser.Builder()
            .withActivity(this@MainActivity)
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .allowCustomPath(true)
            .setType(StorageChooser.DIRECTORY_CHOOSER)
            .build()

        downloadChooser.setOnSelectListener(OnSelectListener { path -> downloadPP(path) })

        downloadChooser.show()

    }

    fun downloadPP(path: String) {
        val gson = GsonBuilder().create()
        val json = AppConfig.retrieve(this)

        val jsonOutput = gson.toJson(json)
        val currentTimestamp = System.currentTimeMillis()

        val fileName = "sms-gateway-${currentTimestamp}.jpg"

        try {
            var bitmap = encodeBitmap(jsonOutput, BarcodeFormat.QR_CODE, 250, 250)

            val file = File(path, fileName)
            val out = FileOutputStream(file)
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.close()
            Toast.makeText(this, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun upload() {
        if (ActivityCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(READ_EXTERNAL_STORAGE),
                    READ
                )
            }
        } else {
            uploadP()
        }
    }

    fun uploadP() {
        val uploadChooser = StorageChooser.Builder()
            .withActivity(this@MainActivity)
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .setType(StorageChooser.FILE_PICKER)
            .allowCustomPath(true)
            .customFilter(listOf("jpg"))
            .build()


        uploadChooser.setOnSelectListener(OnSelectListener { path -> uploadPP(path) })

        uploadChooser.show()
    }

    fun uploadPP(path: String) {
        try {
            var bitmap = BitmapFactory.decodeFile(path)
            var result = decodeBitmap(bitmap)
            jsonToConfig(result?.text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun jsonToConfig(path: String?) {
        val gson = GsonBuilder().create()
        try {
            var appConfig = gson.fromJson<AppConfig>(path, AppConfig::class.java)
            retrieve(appConfig)
            save()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.CAMERA),
                    CAMERA
                )
            }
        } else {
            scanP();
        }
    }

    fun scanP() {
        val intent = Intent(this, CameraActivity::class.java)
        scanResultLauncher.launch(intent)
    }

    private var scanResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val res = data?.getStringExtra("result")
                jsonToConfig(res)
            }
        }

    fun saveP() {
        val c = country.text.toString()
        val a = api.text.toString()
        val o = onesignal.text.toString()
        val auth = authorization.text.toString()
        val enab = enabled.isChecked
        AppConfig.save(AppConfig(enab, o, a, c, auth), this)
    }

    fun save() {
        saveP()
        ProcessPhoenix.triggerRebirth(this);
    }

    private fun retrieve(config: AppConfig) {
        enabled.isChecked = config.enabled
        country.setText(config.country)
        api.setText(config.api)
        onesignal.setText(config.onesignal)
        authorization.setText(config.authorization)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        scanP()
                    } else {
                        Toast.makeText(this, "Permission Denied :(", Toast.LENGTH_LONG).show()
                    }
                    return
                }
            }
            WRITE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        downloadP()
                    } else {
                        Toast.makeText(this, "Permission Denied :(", Toast.LENGTH_LONG).show()
                    }
                    return
                }
            }
            READ -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        uploadP()
                    } else {
                        Toast.makeText(this, "Permission Denied :(", Toast.LENGTH_LONG).show()
                    }
                    return
                }
            }
        }
    }

}