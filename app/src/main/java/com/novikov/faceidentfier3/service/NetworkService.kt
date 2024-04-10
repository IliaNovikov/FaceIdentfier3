package com.novikov.faceidentfier3.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch

class NetworkService {
    private val client = OkHttpClient()

    suspend fun sendPictureFile(file: File): Boolean{
        val countDownLatch = CountDownLatch(1)
        Log.i("network file", file.readBytes().size.toString())
        var result = false
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
//            .addPart(RequestBody.create("image/*".toMediaType(), file))
            .addFormDataPart("file", "face.jpg", RequestBody.create("image/jpg".toMediaType(), file))
            .build()
        val request = Request.Builder()
            .url("http://192.168.1.104:8000/face")
            .post(requestBody)
            .build()

        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("network", e.message.toString())
                result = false
                countDownLatch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("response", response.body!!.string())
                result = response.body!!.string() == "success"
                countDownLatch.countDown()
            }

        })

        withContext(Dispatchers.IO) {
            countDownLatch.await()
        }

        return result

    }

}