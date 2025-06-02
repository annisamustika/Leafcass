package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CassavaClassifier(context: Context) {

        private val interpreter: Interpreter
        private val labels: List<String>
        private val threshold = 0.80f // ambang batas kepercayaan

        init {
            val model = FileUtil.loadMappedFile(context, "singkong3.tflite") // ganti sesuai modelmu
            interpreter = Interpreter(model)
            labels = FileUtil.loadLabels(context, "labels.txt") // file labels.txt di assets
        }

        fun classify(bitmap: Bitmap): Pair<String, Float> {
            val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val input = ByteBuffer.allocateDirect(4 * 224 * 224 * 3).apply {
                order(ByteOrder.nativeOrder())
                rewind()
            }

            for (y in 0 until 224) {
                for (x in 0 until 224) {
                    val pixel = resized.getPixel(x, y)
                    input.putFloat(((pixel shr 16 and 0xFF) / 255.0f))
                    input.putFloat(((pixel shr 8 and 0xFF) / 255.0f))
                    input.putFloat(((pixel and 0xFF) / 255.0f))
                }
            }

            val output = Array(1) { FloatArray(labels.size) }
            interpreter.run(input, output)

            val confidences = output[0]
            val maxIdx = confidences.indices.maxByOrNull { confidences[it] } ?: -1
            val maxConfidence = confidences[maxIdx]

            return labels[maxIdx] to maxConfidence
            }
        }


