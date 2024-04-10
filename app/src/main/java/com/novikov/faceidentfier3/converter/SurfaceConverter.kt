package com.novikov.faceidentfier3.converter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.PixelCopy
import android.view.SurfaceView
import com.novikov.faceidentfier3.service.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.CountDownLatch

class SurfaceConverter(private val context: Context) {

    private var bitmap: Bitmap? = null
//    var handler: Handler? = null
    private var countDownLatch = CountDownLatch(1)
    private var file = File("")

    val listener = object : PixelCopy.OnPixelCopyFinishedListener{
        override fun onPixelCopyFinished(copyResult: Int) {
            if (copyResult == PixelCopy.SUCCESS){
                Log.i("copy", "success")
                countDownLatch = CountDownLatch(1)
//                val fos = context.openFileOutput("face.jpg", Context.MODE_PRIVATE)
////
////                val stream = ByteArrayOutputStream()
////
////                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 60, stream)
////
////                fos.write(stream.toByteArray())
////
////                fos.close()
////
////                val file = File("face.jpg")
////                file.delete()
////                Log.i("file", file.toString())

                Log.i("filesDir", context.filesDir.toString())
                file = File(context.filesDir,"face.jpg")
                file.outputStream().use {
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 60, it)
                }

//                CoroutineScope(Dispatchers.IO).launch {
//                    NetworkService().sendPictureFile(file)
//                }.invokeOnCompletion {
//                    countDownLatch.countDown()
//                }
            }
        }
    }

    suspend fun surfaceToFile(surfaceView: SurfaceView) {

        bitmap = Bitmap.createBitmap(surfaceView.width, surfaceView.height, Bitmap.Config.RGB_565)
        PixelCopy.request(surfaceView.holder.surface, bitmap!!, listener, surfaceView.handler)
//        withContext(Dispatchers.IO) {
//            countDownLatch.await()
//        }
        delay(500)
        Log.i("converter file", file.readBytes().size.toString())
        NetworkService().sendPictureFile(file)
//        Handler().postDelayed({
//            Log.i("handler", "work")
//        }, 100)
//        delay(200)
        Log.i("copy", "finish")
    }
}