package com.airfist.share

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.handstack.HandDetection
import com.google.mlkit.vision.handstack.HandDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var statusText: TextView
    private lateinit var cameraExecutor: ExecutorService
    private val STRATEGY = Strategy.P2P_POINT_TO_POINT // Fast sharing ke liye
    private val SERVICE_ID = "com.airfist.share.SERVICE_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFinder = findViewById(R.id.viewFinder)
        statusText = findViewById(R.id.statusText)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Permissions mangna
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES
        ), 101)

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            // ML Kit Hand Detection logic
            val options = HandDetectorOptions.Builder()
                .setDetectorMode(HandDetectorOptions.STREAM_MODE)
                .build()
            val detector = HandDetection.getClient(options)

            detector.process(image)
                .addOnSuccessListener { hands ->
                    if (hands.isNotEmpty()) {
                        // Agar haath dikha, toh hum use "Fist" gesture maan kar sharing start karenge
                        runOnUiThread {
                            statusText.text = "Status: Fist Detected! Starting Share..."
                            startAdvertising() 
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    // Dusre phone ko dhundne ke liye (Sender)
    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(this)
            .startAdvertising("User_Phone", SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener { Toast.makeText(this, "Searching for nearby phones...", Toast.LENGTH_SHORT).show() }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Nearby.getConnectionsClient(this@MainActivity).acceptConnection(endpointId, payloadCallback)
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                statusText.text = "Status: Connected!"
            }
        }
        override fun onDisconnected(endpointId: String) {
            statusText.text = "Status: Disconnected"
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // File receive hone par yahan logic aayega
            Toast.makeText(this@MainActivity, "File Received!", Toast.LENGTH_SHORT).show()
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }
}
