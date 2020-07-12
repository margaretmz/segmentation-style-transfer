package org.tensorflow.example.tfsegmentstylize.ui.main

import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider

fun ProcessCameraProvider.hasBackCamera(): Boolean {
    return hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
}

fun ProcessCameraProvider.hasFrontCamera(): Boolean {
    return hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
}