package com.e2etflite.sample.backgroundstylizer.di

import com.e2etflite.sample.backgroundstylizer.ui.segmentation.SegmentationAndStyleTransferViewModel
import com.e2etflite.sample.backgroundstylizer.ui.segmentation.StyleTransferModelExecutor
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val segmentationAndStyleTransferModule = module {

    factory { StyleTransferModelExecutor(get(), false) }

    viewModel {
        SegmentationAndStyleTransferViewModel(get())
    }
}