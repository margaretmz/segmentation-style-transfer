package com.e2etflite.sample.backgroundstylizer

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.e2etflite.sample.backgroundstylizer.ui.style.StyleFragment
import com.e2etflite.sample.backgroundstylizer.ui.segmentation.SegmentationAndStyleTransferFragment
import com.e2etflite.sample.backgroundstylizer.ui.segmentation.SegmentationAndStyleTransferFragmentDirections
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity(),
    StyleFragment.OnListFragmentInteractionListener {

    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        title = ""

        // Navigate back to Camera fragment
        imageViewBack.setOnClickListener {
            val action = SegmentationAndStyleTransferFragmentDirections.backHome()
            findNavController(R.id.fragment_container).navigate(action)
        }

    }

    companion object {

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

    override fun onListFragmentInteraction(item: String) {

        // Find fragment and execute method
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val fragment= navHostFragment?.childFragmentManager?.fragments?.get(0)
        (fragment as SegmentationAndStyleTransferFragment).methodToStartStyleTransfer(item)

    }

}