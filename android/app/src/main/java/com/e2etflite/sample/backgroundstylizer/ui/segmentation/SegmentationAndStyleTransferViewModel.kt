package com.e2etflite.sample.backgroundstylizer.ui.segmentation

import android.app.Application
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.get
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter
import org.tensorflow.lite.task.vision.segmenter.OutputType
import org.tensorflow.lite.task.vision.segmenter.Segmentation
import java.io.IOException

class SegmentationAndStyleTransferViewModel(application: Application) :
        AndroidViewModel(application),
        KoinComponent {

    private lateinit var imageSegmenter: ImageSegmenter
    private lateinit var scaledMaskBitmap: Bitmap
    var startTime: Long = 0L
    var inferenceTime = 0L
    lateinit var scaledBitmapObject: Bitmap

    var stylename = String()
    var seekBarProgress: Float = 0F

    private var _currentList: ArrayList<String> = ArrayList()
    val currentList: ArrayList<String>
        get() = _currentList

    private val _totalTimeInference = MutableLiveData<Int>()
    val totalTimeInference: LiveData<Int>
        get() = _totalTimeInference

    private val _styledBitmap = MutableLiveData<ModelExecutionResult>()
    val styledBitmap: LiveData<ModelExecutionResult>
        get() = _styledBitmap

    private val _inferenceDone = MutableLiveData<Boolean>()
    val inferenceDone: LiveData<Boolean>
        get() = _inferenceDone

    val styleTransferModelExecutor: StyleTransferModelExecutor

    init {

        stylename = "mona.JPG"

        _currentList.addAll(application.assets.list("thumbnails")!!)

        styleTransferModelExecutor = get()

    }

    fun setStyleName(string: String) {
        stylename = string
    }

    fun setTheSeekBarProgress(progress: Float) {
        seekBarProgress = progress
    }

    fun onApplyStyle(
            context: Context,
            contentBitmap: Bitmap,
            styleFilePath: String
    ) {

        viewModelScope.launch(Dispatchers.Default) {
            inferenceExecute(contentBitmap, styleFilePath, context)
        }
    }

    private fun inferenceExecute(
            contentBitmap: Bitmap,
            styleFilePath: String,
            context: Context
    ) {

        // Below use Standard interpreter or ML binding...uncomment at your choice
        //******************************
        val result = styleTransferModelExecutor.executeWithMLBinding(contentBitmap, styleFilePath, context)
        //******************************
        _totalTimeInference.postValue(result.totalExecutionTime.toInt())
        _styledBitmap.postValue(result)
        _inferenceDone.postValue(true)
    }

    fun cropPersonFromPhoto(bitmap: Bitmap): Pair<Bitmap?, Long> {
        try {
            // Initialization
            startTime = SystemClock.uptimeMillis()
            val options =
                    ImageSegmenter.ImageSegmenterOptions.builder()
                            .setOutputType(OutputType.CATEGORY_MASK).build()
            imageSegmenter =
                    ImageSegmenter.createFromFileAndOptions(
                            getApplication(),
                            "lite-model_deeplabv3_1_metadata_2.tflite",
                            options
                    )

            // Run inference
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val results: List<Segmentation> = imageSegmenter.segment(tensorImage)
            Log.i("LIST", results[0].toString())
            val result = results[0]
            val tensorMask = result.masks[0]
            Log.i("RESULT", result.coloredLabels.toString())
            val rawMask = tensorMask.tensorBuffer.intArray
            Log.i("NUMBER", rawMask.size.toString())
            Log.i("VALUES", rawMask.contentToString())

            val output = Bitmap.createBitmap(
                    tensorMask.width,
                    tensorMask.height,
                    Bitmap.Config.ARGB_8888
            )
            for (y in 0 until tensorMask.height) {
                for (x in 0 until tensorMask.width) {
                    output.setPixel(
                            x,
                            y,
                            if (rawMask[y * tensorMask.width + x] == 0) Color.TRANSPARENT else Color.BLACK
                    )
                }
            }
            scaledMaskBitmap =
                    Bitmap.createScaledBitmap(output, bitmap.getWidth(), bitmap.getHeight(), true)
            inferenceTime = SystemClock.uptimeMillis() - startTime
        } catch (e: IOException) {
            Log.e("ImageSegmenter", "Error: ", e)
        }

        return Pair(cropBitmapWithMask(bitmap, scaledMaskBitmap), inferenceTime)
    }


    fun cropBitmapWithMask(original: Bitmap, mask: Bitmap?): Bitmap? {
        if (mask == null
        ) {
            return null
        }
        Log.i("ORIGINAL_WIDTH", original.width.toString())
        Log.i("ORIGINAL_HEIGHT", original.height.toString())
        Log.i("MASK_WIDTH", original.width.toString())
        Log.i("MASK_HEIGHT", original.height.toString())
        val w = original.width
        val h = original.height
        if (w <= 0 || h <= 0) {
            return null
        }
        val cropped: Bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        Log.i("CROPPED_WIDTH", cropped.width.toString())
        Log.i("CROPPED_HEIGHT", cropped.height.toString())
        val canvas = Canvas(cropped)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(original, 0f, 0f, null)
        canvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null

        return cropped
    }

    fun cropBitmapWithMaskForStyle(original: Bitmap, mask: Bitmap?): Bitmap? {
        if (mask == null
        ) {
            return null
        }
        val w = original.width
        val h = original.height
        if (w <= 0 || h <= 0) {
            return null
        }

        val scaledBitmap = Bitmap.createScaledBitmap(
                mask,
                w,
                h, true
        )

        val cropped = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cropped)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        canvas.drawBitmap(original, 0f, 0f, null)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
        paint.xfermode = null
        return cropped
    }

    override fun onCleared() {
        super.onCleared()
        styleTransferModelExecutor.close()
    }

}