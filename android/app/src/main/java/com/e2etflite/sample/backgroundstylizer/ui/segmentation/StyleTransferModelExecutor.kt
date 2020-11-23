package com.e2etflite.sample.backgroundstylizer.ui.segmentation

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.e2etflite.sample.backgroundstylizer.ml.MagentaArbitraryImageStylizationV1256Fp16Prediction1
import com.e2etflite.sample.backgroundstylizer.ml.MagentaArbitraryImageStylizationV1256Fp16Transfer1
import com.e2etflite.sample.backgroundstylizer.utils.ImageUtils
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model

data class ModelExecutionResult(
        val styledImage: Bitmap,
        val preProcessTime: Long = 0L,
        val stylePredictTime: Long = 0L,
        val styleTransferTime: Long = 0L,
        val postProcessTime: Long = 0L,
        val totalExecutionTime: Long = 0L,
        val executionLog: String = "",
        val errorMessage: String = ""
)

@SuppressWarnings("GoodTime")
class StyleTransferModelExecutor(
        context: Context,
        private var useGPU: Boolean = false
) {

    private var numberThreads = 4
    private var fullExecutionTime = 0L
    private var preProcessTime = 0L
    private var stylePredictTime = 0L
    private var styleTransferTime = 0L
    private var postProcessTime = 0L
    private var modelMlBindingPredict: MagentaArbitraryImageStylizationV1256Fp16Prediction1
    private var modelMlBindingTransfer: MagentaArbitraryImageStylizationV1256Fp16Transfer1

    init {

        // ML binding set number of threads or GPU for accelerator
        /*val compatList = CompatibilityList()
        val options = if(compatList.isDelegateSupportedOnThisDevice) {
            Log.d(TAG, "This device is GPU Compatible ")
            Model.Options.Builder().setDevice(Model.Device.GPU).build()
        } else {
            Log.d(TAG, "This device is not GPU Incompatible ")
            Model.Options.Builder().setNumThreads(4).build()
        }*/

        val options = Model.Options.Builder().setNumThreads(4).build()
        modelMlBindingPredict = MagentaArbitraryImageStylizationV1256Fp16Prediction1.newInstance(context, options)
        modelMlBindingTransfer = MagentaArbitraryImageStylizationV1256Fp16Transfer1.newInstance(context, options)

    }

    companion object {
        private const val TAG = "StyleTransferMExec"
        private const val STYLE_IMAGE_SIZE = 256
        private const val CONTENT_IMAGE_SIZE = 384
        private const val BOTTLENECK_SIZE = 100
    }

    // Function for ML Binding
    fun executeWithMLBinding(
            contentImagePath: Bitmap,
            styleImageName: String,
            context: Context
    ): ModelExecutionResult {
        try {
            Log.i(TAG, "running models")

            fullExecutionTime = SystemClock.uptimeMillis()

            preProcessTime = SystemClock.uptimeMillis()
            // Creates inputs for reference.
            val styleBitmap = ImageUtils.loadBitmapFromResources(context, "thumbnails/$styleImageName")
            val styleImage = TensorImage.fromBitmap(styleBitmap)
            val contentImage = TensorImage.fromBitmap(contentImagePath)
            preProcessTime = SystemClock.uptimeMillis() - preProcessTime

            stylePredictTime = SystemClock.uptimeMillis()
            // The results of this inference could be reused given the style does not change
            // That would be a good practice in case this was applied to a video stream.
            // Runs model inference and gets result.
            val outputsPredict = modelMlBindingPredict.process(styleImage)
            val styleBottleneckPredict = outputsPredict.styleBottleneckAsTensorBuffer
            stylePredictTime = SystemClock.uptimeMillis() - stylePredictTime
            Log.d(TAG, "Style Predict Time to run: $stylePredictTime")

            styleTransferTime = SystemClock.uptimeMillis()
            // Runs model inference and gets result.
            val outputs = modelMlBindingTransfer.process(contentImage, styleBottleneckPredict)
            styleTransferTime = SystemClock.uptimeMillis() - styleTransferTime
            Log.d(TAG, "Style apply Time to run: $styleTransferTime")

            postProcessTime = SystemClock.uptimeMillis()
            val styledImage = outputs.styledImageAsTensorImage
            val styledImageBitmap = styledImage.bitmap
            postProcessTime = SystemClock.uptimeMillis() - postProcessTime

            fullExecutionTime = SystemClock.uptimeMillis() - fullExecutionTime
            Log.d(TAG, "Time to run everything: $fullExecutionTime")

            return ModelExecutionResult(
                    styledImageBitmap,
                    preProcessTime,
                    stylePredictTime,
                    styleTransferTime,
                    postProcessTime,
                    fullExecutionTime,
                    formatExecutionLog()
            )
        } catch (e: Exception) {
            val exceptionLog = "something went wrong: ${e.message}"
            Log.d(TAG, exceptionLog)

            val emptyBitmap =
                    ImageUtils.createEmptyBitmap(
                            CONTENT_IMAGE_SIZE,
                            CONTENT_IMAGE_SIZE
                    )
            return ModelExecutionResult(
                    emptyBitmap, errorMessage = e.message!!
            )
        }
    }

    private fun formatExecutionLog(): String {
        val sb = StringBuilder()
        sb.append("Input Image Size: $CONTENT_IMAGE_SIZE x $CONTENT_IMAGE_SIZE\n")
        sb.append("GPU enabled: $useGPU\n")
        sb.append("Number of threads: $numberThreads\n")
        sb.append("Pre-process execution time: $preProcessTime ms\n")
        sb.append("Predicting style execution time: $stylePredictTime ms\n")
        sb.append("Transferring style execution time: $styleTransferTime ms\n")
        sb.append("Post-process execution time: $postProcessTime ms\n")
        sb.append("Full execution time: $fullExecutionTime ms\n")
        return sb.toString()
    }

    fun close() {
        modelMlBindingPredict.close()
        modelMlBindingTransfer.close()
    }
}
