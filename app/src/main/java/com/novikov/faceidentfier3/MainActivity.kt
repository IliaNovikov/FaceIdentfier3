package com.novikov.faceidentfier3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.FaceDetectionListener
import android.hardware.Camera.open
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PersistableBundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.TextureView
import android.widget.Toast
import com.novikov.faceidentfier3.converter.SurfaceConverter
import com.novikov.faceidentfier3.databinding.ActivityMainBinding
import com.novikov.faceidentfier3.service.NetworkService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener, SurfaceHolder.Callback {

    private lateinit var binding: ActivityMainBinding
    private var camera: Camera? = null
    private var canvas = Canvas()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val surfaceConverter = SurfaceConverter(this)
    private val handlerThread = HandlerThread("converter")
    private var isWriting = false
    private val networkService = NetworkService()

    private val faceDetectionListener = object : FaceDetectionListener{
        override fun onFaceDetection(faces: Array<out Camera.Face>?, camera: Camera?) {
            Log.i("faceCount", faces?.size.toString())
            if (faces?.size!! > 0){
                val rectangle = faces[0].rect
                canvas = binding.tvMain.lockCanvas()!!
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                rectangle.offset(-800, -600)
                canvas.drawRect(-rectangle.exactCenterY() - 350f,
                    -rectangle.exactCenterX() - 350f,
                    -rectangle.exactCenterY() + 350f,
                    -rectangle.exactCenterX() + 350f,
                    paint)

                Log.i("rect centerX", rectangle.exactCenterX().toString())
                Log.i("rect centerY", rectangle.exactCenterY().toString())
                binding.tvMain.unlockCanvasAndPost(canvas)

                if (!isWriting){
                    isWriting = true
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000)
                        surfaceConverter.surfaceToFile(binding.svMain)

                    }.invokeOnCompletion {
                        Log.i("gv", GlobalValues.requestResult.toString())
                        try{
                            runOnUiThread {
                                if(GlobalValues.requestResult)
                                    Toast.makeText(this@MainActivity, "Успешно", Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(this@MainActivity, "Ошибка", Toast.LENGTH_SHORT).show()
                            }
                        }
                        catch (ex: Exception){
                            Log.e("toast", ex.message.toString())
                        }

                        isWriting = false
                    }
                }
            }

            else{
                canvas = binding.tvMain.lockCanvas()!!
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                binding.tvMain.unlockCanvasAndPost(canvas)
            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        paint.setColor(Color.GREEN)

//        handlerThread.start()
//        bitmapConverter.handler = Handler(handlerThread.looper)

        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET), 1)
            Toast.makeText(this, "Необходимо дать все разрешения", Toast.LENGTH_LONG).show()
        }

        binding.svMain.holder.addCallback(this)
        binding.tvMain.surfaceTextureListener = this
//        Handler().postDelayed(object: Runnable{
//            override fun run() {
//                binding.tvMain.surfaceTextureListener = this@MainActivity
//            }
//
//        }, 2000)


        setContentView(binding.root)
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                startActivity(MainActivity().intent)
                this.recreate()
                return
            }
            else{
                Toast.makeText(this, "Нужно предоставить все разрешения", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.i("texture", "available")
//        val canvas2 = binding.tvMain.lockCanvas()
//        canvas2?.drawCircle(40f,40f,40f, paint)
//        binding.tvMain.unlockCanvasAndPost(canvas2!!)

////        camera = open(Camera.getNumberOfCameras()-1)
//        camera!!.setPreviewTexture(surface)
//        camera!!.setDisplayOrientation(90)
////        camera!!.startPreview()
////        camera!!.setFaceDetectionListener(faceDetectionListener)
////        camera!!.startFaceDetection()

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
//        TODO("Not yet implemented")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
//        TODO("Not yet implemented")
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//        TODO("Not yet implemented")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.i("surface", "created")
        camera = open(Camera.getNumberOfCameras()-1)
        camera!!.parameters.setRotation(90)
        camera!!.setPreviewDisplay(holder)
        camera!!.setDisplayOrientation(90)
        camera!!.startPreview()
        camera!!.setFaceDetectionListener(faceDetectionListener)
        camera!!.startFaceDetection()
//        canvas = holder.lockCanvas(null)
        Log.i("canvas", canvas.toString())
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//        TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        TODO("Not yet implemented")
    }
}