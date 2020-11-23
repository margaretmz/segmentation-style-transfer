package com.e2etflite.sample.backgroundstylizer.ui.camera

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.e2etflite.sample.backgroundstylizer.MainActivity.Companion.getOutputDirectory
import com.e2etflite.sample.backgroundstylizer.R
import com.e2etflite.sample.backgroundstylizer.databinding.FragmentCameraBinding
import com.e2etflite.sample.backgroundstylizer.utils.ImageUtils
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A simple [Fragment] subclass that captures and saves a photo with CameraX
 */
class CameraFragment : Fragment() {

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var bitmap: Bitmap? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private lateinit var cameraSwitchButton: ImageView
    private lateinit var cameraCaptureButton: Button
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var binding: FragmentCameraBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.galleryButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                    PICK_IMAGE_REQUEST
            )
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraSwitchButton = view.findViewById(R.id.camera_switch_button)
        cameraCaptureButton = view.findViewById(R.id.camera_capture_button)

        // Set camera switch
        cameraSwitchButton.setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            // Re-bind use cases to update selected camera
            startCamera()
        }

        // Setup the listener for take photo button
        cameraCaptureButton.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory(requireContext())

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        val screenAspectRatio = 1.0 / 1.0
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // set up Preview
            preview = Preview.Builder().build()

            // set up Capture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(Surface.ROTATION_270)
                .setTargetRotation(viewFinder.display.rotation)
                .setTargetResolution(Size(512, 512)) // Target resolution to 512x512
                .build()

            // Select front camera as default for selfie
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing)
                .build() // Change camera to front facing

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                preview?.setSurfaceProvider(viewFinder.createSurfaceProvider(camera?.cameraInfo))
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create timestamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                    FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                    // Get rotation degree
                    val degrees: Int = rotationDegrees(photoFile)

                    // Create a bitmap from the .jpg image
                    bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                    // Rotate image if needed
                    if (degrees != 0) {
                        bitmap = rotateBitmap(bitmap!!, degrees)
                    }

                    // Save bitmap image
                    val filePath = ImageUtils.saveBitmap(bitmap, photoFile)
                    Log.e(TAG, filePath)

                    // Pass the file path to the next screen
                    val action =
                            CameraFragmentDirections.actionCameraToSelfie2segmentation(filePath)
                    findNavController().navigate(action)

                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {

            val filePath: Uri? = data.data
            if (filePath != null) {

                val action =
                        CameraFragmentDirections.actionCameraToSelfie2segmentation(
                                filePath.toString()
                        )
                findNavController().navigate(action)
            }
        }
    }

    /**
     * Get rotation degree from image exif
     */
    private fun rotationDegrees(file: File): Int {
        val ei = ExifInterface(file.absolutePath);
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        // Return rotation degree based on orientation from exif
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {

        val rotationMatrix = Matrix()
        rotationMatrix.postRotate(rotationDegrees.toFloat())
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotationMatrix, true)
        bitmap.recycle()

        return rotatedBitmap
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PICK_IMAGE_REQUEST = 1024
    }

}