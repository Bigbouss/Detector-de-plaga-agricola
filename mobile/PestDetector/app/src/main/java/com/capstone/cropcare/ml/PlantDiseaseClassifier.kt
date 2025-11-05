//package com.capstone.cropcare.ml
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.util.Log
//import org.tensorflow.lite.Interpreter
//import org.tensorflow.lite.support.common.FileUtil
//import org.tensorflow.lite.support.image.TensorImage
//import org.tensorflow.lite.support.image.ops.ResizeOp
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//
//import kotlin.math.roundToInt
//
//data class Classification(
//    val label: String,
//    val confidence: Float
//)
//
//class PlantDiseaseClassifier(private val context: Context) {
//
//    private var interpreter: Interpreter? = null
//    private var labels: List<String> = emptyList()
//
//    // Configuración del modelo (ajusta según tu modelo)
//    private val inputImageWidth = 224 // Cambia según tu modelo
//    private val inputImageHeight = 224
//    private val numClasses = 12 // 11 enfermedades + Unknown
//
//    companion object {
//        private const val MODEL_PATH = "plant_model.tflite"
//        private const val LABELS_PATH = "labels.txt"
//        private const val TAG = "PlantDiseaseClassifier"
//    }
//
//    init {
//        setupInterpreter()
//        loadLabels()
//    }
//
//    private fun setupInterpreter() {
//        try {
//            val options = Interpreter.Options().apply {
//                setNumThreads(4)
//            }
//
//            val model = FileUtil.loadMappedFile(context, MODEL_PATH)
//            interpreter = Interpreter(model, options)
//
//            // Verificar dimensiones del modelo
//            val inputShape = interpreter?.getInputTensor(0)?.shape()
//            val outputShape = interpreter?.getOutputTensor(0)?.shape()
//
//            Log.d(TAG, "Modelo cargado correctamente")
//            Log.d(TAG, "Input shape: ${inputShape?.contentToString()}")
//            Log.d(TAG, "Output shape: ${outputShape?.contentToString()}")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error cargando el modelo", e)
//        }
//    }
//
//    private fun loadLabels() {
//        try {
//            val inputStream = context.assets.open(LABELS_PATH)
//            val reader = BufferedReader(InputStreamReader(inputStream))
//            labels = reader.readLines()
//            reader.close()
//
//            Log.d(TAG, "Labels cargados: ${labels.size} clases")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error cargando labels", e)
//        }
//    }
//
//    fun classify(bitmap: Bitmap): Classification? {
//        if (interpreter == null) {
//            Log.e(TAG, "Interpreter no inicializado")
//            return null
//        }
//
//        try {
//            // Preprocesar imagen
//            val resizedBitmap = Bitmap.createScaledBitmap(
//                bitmap,
//                inputImageWidth,
//                inputImageHeight,
//                true
//            )
//
//            // Convertir a ByteBuffer
//            val inputBuffer = bitmapToByteBuffer(resizedBitmap)
//
//            // Preparar output
//            val output = Array(1) { FloatArray(numClasses) }
//
//            // Ejecutar inferencia
//            interpreter?.run(inputBuffer, output)
//
//            // Procesar resultados
//            val results = output[0]
//
//            // Log de todas las predicciones
//            Log.d(TAG, "=== Todas las predicciones ===")
//            results.forEachIndexed { index, confidence ->
//                if (confidence > 0.01f) { // Solo mostrar si > 1%
//                    Log.d(TAG, "${labels.getOrNull(index)}: ${(confidence * 100).format(2)}%")
//                }
//            }
//
//            val maxIndex = results.indices.maxByOrNull { results[it] } ?: 0
//            val confidence = results[maxIndex]
//
//            Log.d(TAG, "=== Resultado final ===")
//            Log.d(TAG, "Clasificación: ${labels[maxIndex]} (${(confidence * 100).format(2)}%)")
//
//            return Classification(
//                label = labels.getOrNull(maxIndex) ?: "Unknown",
//                confidence = confidence
//            )
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error en clasificación", e)
//            return null
//        }
//    }
//
//    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
//        val byteBuffer = ByteBuffer.allocateDirect(4 * inputImageWidth * inputImageHeight * 3)
//        byteBuffer.order(ByteOrder.nativeOrder())
//
//        val intValues = IntArray(inputImageWidth * inputImageHeight)
//        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
//
//        var pixel = 0
//        for (i in 0 until inputImageWidth) {
//            for (j in 0 until inputImageHeight) {
//                val value = intValues[pixel++]
//
//                // Normalización [0, 1] - igual que rescale=1./255 en Python
//                byteBuffer.putFloat(((value shr 16 and 0xFF) / 255.0f))  // R
//                byteBuffer.putFloat(((value shr 8 and 0xFF) / 255.0f))   // G
//                byteBuffer.putFloat(((value and 0xFF) / 255.0f))         // B
//            }
//        }
//
//        return byteBuffer
//    }
//
//    fun close() {
//        interpreter?.close()
//        interpreter = null
//    }
//}
//
//// Extension para formatear floats
//private fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)