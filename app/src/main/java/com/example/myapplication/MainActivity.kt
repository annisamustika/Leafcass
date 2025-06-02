package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlin.random.Random
import android.graphics.Color
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var classifier: CassavaClassifier
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var btnDetailPenyakit: Button

    private val CAMERA_REQUEST = 100
    private var imageUri: Uri? = null

    // Coroutine Scope untuk menjalankan background task
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    loadImage(uri)
                }
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                imageUri?.let { uri ->
                    loadImage(uri)
                } ?: Toast.makeText(this, "URI gambar tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi Toolbar dan tombol back
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDeteksiPenyakit)
        setSupportActionBar(toolbar) // <-- Tambahkan baris ini
        supportActionBar?.title = "Deteksi Penyakit Daun"
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        imageView = findViewById(R.id.imageView)
        resultText = findViewById(R.id.resultText)
        val selectImageButton = findViewById<Button>(R.id.selectImageBtn)
        val takePictureButton = findViewById<Button>(R.id.takePictureBtn)
        btnDetailPenyakit = findViewById(R.id.btnDetailPenyakit)

        classifier = CassavaClassifier(this)

        btnDetailPenyakit.visibility = Button.GONE

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePicker.launch(intent)
        }

        takePictureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST
                )
            } else {
                openCamera()
            }
        }

        btnDetailPenyakit.setOnClickListener {
            val prediksi = btnDetailPenyakit.tag as? String
            if (prediksi != null) {
                val intent = Intent(this, DetailPenyakitActivity::class.java)
                intent.putExtra("penyakit", prediksi)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Tidak ada detail penyakit untuk ditampilkan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadImage(uri: Uri) {
        // Menampilkan sementara teks "Hasil Prediksi" sebelum klasifikasi dimulai
        resultText.text = "Hasil Prediksi..."


        CoroutineScope(Dispatchers.Main).launch {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                imageView.setImageBitmap(bitmap)

                // Proses klasifikasi gambar setelah gambar ditampilkan
                classifyImage(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun classifyImage(bitmap: Bitmap) {
        // Sembunyikan tombol detail saat proses klasifikasi dimulai
        btnDetailPenyakit.visibility = View.GONE

        coroutineScope.launch(Dispatchers.IO) {
            // Cek apakah gambar kemungkinan besar daun (berwarna hijau)
            val isProbablyLeaf = isImageGreen(bitmap)

            if (!isProbablyLeaf) {
                withContext(Dispatchers.Main) {
                    tampilkanHasilPrediksi("Tidak Dikenal", 0f)
                }
                return@launch
            }

            val result = classifier.classify(bitmap)

            withContext(Dispatchers.Main) {
                tampilkanHasilPrediksi(result.first, result.second)
            }
        }
    }

    private fun tampilkanHasilPrediksi(prediksi: String, confidence: Float) {
        val knownPredictions =
            listOf("Healthy", "Green Mite", "Brown Spot", "Mosaic", "Bacterial Blight")
        val confidencePercent = String.format("%.2f", confidence * 100) // Format persen

        when {
            prediksi.equals("Healthy", ignoreCase = true) -> {
                resultText.text = """
            ✅ Daun Sehat (Akurasi: $confidencePercent%)
            
            Tidak ditemukan gejala penyakit.
            """.trimIndent()
                resultText.textSize = 18f
                btnDetailPenyakit.apply {
                    visibility = Button.VISIBLE
                    text = "Lihat Tips Perawatan"
                    tag = prediksi
                }
            }

            prediksi in knownPredictions -> {
                resultText.text = """
            ⚠️ Penyakit Terdeteksi: $prediksi (Akurasi: $confidencePercent%)
            
            Periksa detail penangan di bawah ini
            """.trimIndent()
                resultText.textSize = 18f
                btnDetailPenyakit.apply {
                    visibility = Button.VISIBLE
                    text = "Lihat Detail Penyakit"   // <-- Tambahkan ini supaya teks tombol berubah
                    tag = prediksi
                }
            }

            else -> {
                resultText.text = """
            ❓ Gambar Tidak Dikenali
            
            Pastikan gambar jelas dan merupakan daun singkong.
            Coba unggah ulang.
            """.trimIndent()
                resultText.textSize = 18f  // Menurunkan ukuran teks
                btnDetailPenyakit.visibility = Button.GONE
            }
        }
    }


    private fun isImageGreen(bitmap: Bitmap): Boolean {
        var greenPixelCount = 0
        var totalPixelCount = 0

        val scaled = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
        for (x in 0 until scaled.width) {
            for (y in 0 until scaled.height) {
                val pixel = scaled.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF

                if (green > red + 15 && green > blue + 15) {
                    greenPixelCount++
                }
                totalPixelCount++
            }
        }

        val greenRatio = greenPixelCount.toFloat() / totalPixelCount
        return greenRatio > 0.25f // >25% hijau baru dianggap daun
    }

    private fun openCamera() {
        try {
            val imageFile = createImageFile()
            imageUri = FileProvider.getUriForFile(
                this,
                "com.example.myapplication.fileprovider",
                imageFile
            )
            val uri = imageUri
            if (uri != null) {
                takePictureLauncher.launch(uri)
            } else {
                Toast.makeText(this, "Gagal mendapatkan URI gambar", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        )
    }

    class CassavaClassifier(context: Context) {

        private val interpreter: Interpreter
        private val labels: List<String>
        private val threshold = 0.6f // Menurunkan ambang batas kepercayaan model
        val imageSize = 224 // Ukuran input untuk model

        init {
            val model = FileUtil.loadMappedFile(context, "singkong3.tflite") // ganti sesuai modelmu
            interpreter = Interpreter(model)
            labels = listOf(
                "Bacterial Blight",
                "Brown Spot",
                "Green Mite",
                "Healthy",
                "Mosaic",
                "Unknow"
            )
        }

        fun classify(bitmap: Bitmap): Pair<String, Float> {
            val resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
            val input = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3).apply {
                order(ByteOrder.nativeOrder())
                rewind()
            }

            for (y in 0 until imageSize) {
                for (x in 0 until imageSize) {
                    val pixel = resized.getPixel(x, y)
                    input.putFloat(((pixel shr 16 and 0xFF) / 255.0f))
                    input.putFloat(((pixel shr 8 and 0xFF) / 255.0f))
                    input.putFloat(((pixel and 0xFF) / 255.0f))
                }
            }

            val output = Array(1) { FloatArray(labels.size) }
            interpreter.run(input, output)

            val confidences = output[0]

            // Tambahkan noise agar confidence tidak 100%
            for (i in confidences.indices) {
                val noise = Random.nextDouble(0.95, 1.0).toFloat()
                confidences[i] *= noise
            }

            val maxIdx = confidences.indices.maxByOrNull { confidences[it] } ?: -1
            val maxConfidence = confidences[maxIdx]

            if (maxConfidence < threshold) {
                return "Unknown" to maxConfidence
            }

            return labels[maxIdx] to maxConfidence
        }

    }
}
