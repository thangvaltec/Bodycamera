package com.bodycamera.ba.activity

import android.util.Log
import com.bodycamera.tests.BuildConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class DeviceApiClient {

    private val client = OkHttpClient()
    private val baseUrl = BuildConfig.DEVICE_API_BASE_URL
    private val authPath = BuildConfig.DEVICE_API_AUTHMODE_PATH

    /**
     * サーバーにシリアルを送り、authMode を取得する
     */
    fun getAuthMode(serial: String, callback: (Int?) -> Unit) {
        val json = JSONObject().apply { put("serialNo", serial) }

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(baseUrl + authPath)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API", "Send serial failed: $e")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                Log.d("API", "Response: $result")

                try {
                    val jsonObj = JSONObject(result)
                    val authMode = jsonObj.getInt("authMode")
                    callback(authMode)
                } catch (e: Exception) {
                    Log.e("API", "Parse error: $e")
                    callback(null)
                }
            }
        })
    }
}
