package com.capstone.cropcare.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer

class PlantClassifier(
    private val context: Context,
    private val cropType: String // "corn", "potato"
) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    private val inputSize = 224

    // Procesador de imágenes
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    init {
        loadModel()
        loadLabels()
    }

    private fun loadModel() {
        try {
            val modelPath = when (cropType.lowercase()) {
                "manzanas", "apple" -> "models/AppleModel.tflite"
                "maiz", "corn" -> "models/CornModel.tflite"
                "papas", "potato" -> "models/PotatoModel.tflite"
                else -> "models/CornModel.tflite" // Default
            }

            val model = FileUtil.loadMappedFile(context, modelPath)
            interpreter = Interpreter(model)

            Log.d("PlantClassifier", "Modelo cargado: $modelPath")
        } catch (e: Exception) {
            Log.e("PlantClassifier", "Error cargando modelo", e)
        }
    }

    /**
     * Carga las etiquetas desde assets
     */
    private fun loadLabels() {
        try {
            val labelPath = when (cropType.lowercase()) {
                "manzanas", "apple" -> "labels/AppleLabels.txt"
                "maiz", "corn" -> "labels/CornLabels.txt"
                "papas", "potato" -> "labels/PotatoLabels.txt"
                else -> "labels/CornLabels.txt" // Default
            }

            val reader = BufferedReader(InputStreamReader(context.assets.open(labelPath)))
            labels = reader.readLines()
            reader.close()

            Log.d("PlantClassifier", "Labels cargados: ${labels.size} clases")
            Log.d("PlantClassifier", "Labels: $labels")
        } catch (e: Exception) {
            Log.e("PlantClassifier", "Error cargando labels", e)
        }
    }

    /**
     * Clasifica una imagen de planta
     */
    fun classify(bitmap: Bitmap): ClassificationResult {
        if (interpreter == null || labels.isEmpty()) {
            return ClassificationResult(
                label = "Error",
                confidence = 0f,
                isPlague = false,
                allScores = emptyMap()
            )
        }

        try {
            // 1. Preprocesar imagen
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val processedImage = imageProcessor.process(tensorImage)

            // 2. Preparar salida
            val outputArray = Array(1) { FloatArray(labels.size) }

            // 3. Ejecutar inferencia
            interpreter?.run(processedImage.buffer, outputArray)

            // 4. Procesar resultados
            val scores = outputArray[0]
            val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: 0
            val maxScore = scores[maxIndex]
            val predictedLabel = labels[maxIndex]

            // 5. Determinar si es plaga
            val isPlague = isPlaguePrediction(predictedLabel)

            // Crear mapa de todas las predicciones
            val allScores = labels.mapIndexed { index, label ->
                label to scores[index]
            }.toMap()

            Log.d("PlantClassifier", "🔍 Predicción: $predictedLabel (${(maxScore * 100).toInt()}%)")
            Log.d("PlantClassifier", "   Es plaga: $isPlague")

            return ClassificationResult(
                label = predictedLabel,
                confidence = maxScore,
                isPlague = isPlague,
                allScores = allScores
            )

        } catch (e: Exception) {
            Log.e("PlantClassifier", "Error en clasificación", e)
            return ClassificationResult(
                label = "Error: ${e.message}",
                confidence = 0f,
                isPlague = false,
                allScores = emptyMap()
            )
        }
    }

    private fun isPlaguePrediction(label: String): Boolean {
        val healthyKeywords = listOf(
            "healthy", "sano", "sana", "normal"
        )

        return !healthyKeywords.any { keyword ->
            label.contains(keyword, ignoreCase = true)
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d("PlantClassifier", "🔚 Clasificador cerrado")
    }
}

data class ClassificationResult(
    val label: String,
    val confidence: Float,
    val isPlague: Boolean,
    val allScores: Map<String, Float>
)