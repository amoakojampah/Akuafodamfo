package com.example.akuafodamfo.activities

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.akuafodamfo.databinding.ActivityPestDetectionBinding
import com.example.akuafodamfo.utils.*
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.example.akuafodamfo.BuildConfig
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
class PestDetectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPestDetectionBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var currentBitmap: Bitmap? = null

    private val apiKey: String by lazy {
        if (BuildConfig.OPENAI_API_KEY.isNotEmpty()) {
            BuildConfig.OPENAI_API_KEY
        } else {
            Log.e(TAG, "API key is not configured")
            runOnUiThread {
                Toast.makeText(
                    this@PestDetectionActivity,
                    "API configuration error - please contact support",
                    Toast.LENGTH_LONG
                ).show()
            }
            "" // This will cause the API call to fail with 401
        }
    }

    private val baseUrl = "https://api.openai.com/v1/"

    companion object {
        private const val TAG = "PestDetectionActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val MAX_IMAGE_SIZE = 1024
        private const val IMAGE_QUALITY = 80
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                loadImageFromUri(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image from gallery", e)
                Toast.makeText(
                    this@PestDetectionActivity,
                    "Failed to load image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPestDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissions()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupUI() {
        binding.btnCapture.setOnClickListener { takePhoto() }
        binding.btnUpload.setOnClickListener { selectImageFromGallery() }
        binding.btnAnalyze.setOnClickListener {
            currentBitmap?.let { bitmap ->
                if (apiKey.isNotEmpty()) {
                    analyzeImage(bitmap)
                } else {
                    Toast.makeText(
                        this,
                        "API not configured properly",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } ?: run {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    runOnUiThread {
                        Toast.makeText(
                            this@PestDetectionActivity,
                            "Capture failed: ${exc.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        try {
                            val options = BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                            contentResolver.openInputStream(uri)?.use { stream ->
                                BitmapFactory.decodeStream(stream, null, options)
                            }

                            options.inSampleSize = calculateInSampleSize(
                                options,
                                MAX_IMAGE_SIZE,
                                MAX_IMAGE_SIZE
                            )
                            options.inJustDecodeBounds = false

                            contentResolver.openInputStream(uri)?.use { stream ->
                                val bitmap = BitmapFactory.decodeStream(stream, null, options)
                                if (bitmap != null) {
                                    currentBitmap = bitmap
                                    runOnUiThread {
                                        binding.cameraPreview.visibility = View.GONE
                                        binding.imagePreview.visibility = View.VISIBLE
                                        binding.imagePreview.setImageBitmap(bitmap)
                                        Toast.makeText(
                                            this@PestDetectionActivity,
                                            "Image captured",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    throw Exception("Failed to decode bitmap")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing captured image", e)
                            runOnUiThread {
                                Toast.makeText(
                                    this@PestDetectionActivity,
                                    "Error processing image",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        )
    }

    private fun selectImageFromGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun loadImageFromUri(uri: Uri) {
        var inputStream: InputStream? = null
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            options.inSampleSize = calculateInSampleSize(
                options,
                MAX_IMAGE_SIZE,
                MAX_IMAGE_SIZE
            )
            options.inJustDecodeBounds = false

            inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                ?: throw Exception("Failed to decode bitmap")

            currentBitmap = bitmap
            binding.cameraPreview.visibility = View.GONE
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreview.setImageBitmap(bitmap)
        } catch (e: Exception) {
            throw Exception("Failed to load image: ${e.message}")
        } finally {
            inputStream?.close()
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun analyzeImage(bitmap: Bitmap) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvResult.text = "Analyzing image for pests..."

        try {
            val resizedBitmap = resizeBitmap(bitmap, MAX_IMAGE_SIZE)
            val byteArray = ByteArrayOutputStream().apply {
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, this)
            }.toByteArray()

            val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            val imageUrl = "data:image/jpeg;base64,$base64Image"

            val request = OpenAIRequest(
                model = "gpt-4-vision-preview",
                messages = listOf(
                    RequestMessage(
                        role = "user",
                        content = listOf(
                            ContentItem(
                                type = "text",
                                text = "Analyze this agricultural image and identify any visible pests or diseases. " +
                                        "Provide: 1) Pest/disease name, 2) Severity, 3) Organic treatment options. " +
                                        "Format as bullet points."
                            ),
                            ContentItem(
                                type = "image_url",
                                image_url = ImageUrl(url = imageUrl)
                            )
                        )
                    )
                ),
                max_tokens = 1000
            )

            val requestBody = Gson().toJson(request)
                .toRequestBody("application/json".toMediaTypeOrNull())

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(OpenAIVisionService::class.java)

            if (apiKey.isEmpty()) {
                binding.progressBar.visibility = View.GONE
                binding.tvResult.text = "API key not configured"
                return
            }

            service.analyzeImage("Bearer $apiKey", requestBody).enqueue(
                object : Callback<OpenAIResponse> {
                    override fun onResponse(
                        call: Call<OpenAIResponse>,
                        response: Response<OpenAIResponse>
                    ) {
                        binding.progressBar.visibility = View.GONE

                        if (response.isSuccessful) {
                            val result = response.body()?.choices?.firstOrNull()?.message?.content
                                ?: "No analysis available"
                            binding.tvResult.text = result
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "No error details"
                            Log.e(TAG, "API Error: $errorBody")
                            binding.tvResult.text = when (response.code()) {
                                400 -> "Invalid request. Check image format."
                                401 -> "API key invalid or missing"
                                429 -> "Rate limit exceeded"
                                else -> "Error ${response.code()}: $errorBody"
                            }
                        }
                    }

                    override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                        binding.progressBar.visibility = View.GONE
                        binding.tvResult.text = "Network error: ${t.message}"
                        Log.e(TAG, "API call failed", t)
                    }
                }
            )
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            binding.tvResult.text = "Error: ${e.message}"
            Log.e(TAG, "Image analysis failed", e)
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        if (ratio > 1) {
            width = maxSize
            height = (width / ratio).toInt()
        } else {
            height = maxSize
            width = (height * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentBitmap?.recycle()
        cameraExecutor.shutdown()
    }
}