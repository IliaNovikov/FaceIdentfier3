package com.novikov.faceidentfier3.service

import android.util.Log
import com.novikov.faceidentfier3.GlobalValues
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
//            .url("http://213.5.109.61:9090/face_analytics/predict")
            .url("http://10.100.50.10:8000/face_recognition")
//            .url("http://10.100.50.10:8000/access")
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
//                Log.i("response", response.body!!.string())
                result = response.body!!.string() == "\"success\""
                if (result){
                    val access_request = Request.Builder()
                        .url("http://10.100.50.10:8000/access")
                        .get()
                        .build()

                    val access_call = client.newCall(access_request)

                    access_call.enqueue(object : Callback{
                        override fun onFailure(call: Call, e: IOException) {
                            Log.i("call", "failure")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            Log.i("call", "response")
                        }

                    })
                }
//                Log.i("status", response.body!!.string().toString())
                countDownLatch.countDown()
            }
        })

        withContext(Dispatchers.IO) {
            countDownLatch.await()
        }

//        Log.i("result", result.toString())
        GlobalValues.requestResult = result

        return result

    }

}