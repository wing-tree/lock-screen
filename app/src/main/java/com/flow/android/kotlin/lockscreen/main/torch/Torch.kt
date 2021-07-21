package com.flow.android.kotlin.lockscreen.main.torch

import android.content.Context
import android.graphics.PorterDuff
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.ActivityMainBinding
import com.flow.android.kotlin.lockscreen.util.hideRipple
import com.flow.android.kotlin.lockscreen.util.scale
import com.flow.android.kotlin.lockscreen.util.showRipple

class Torch(private val viewBinding: ActivityMainBinding) {
    object Mode {
        const val On = 0
        const val Off = 1
    }

    private val context = viewBinding.root.context
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var _mode = Mode.Off
    val mode: Int
        get() = _mode

    init {
        cameraId = cameraId()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    if (enabled)
                        on()
                    else
                        off()
                }

                override fun onTorchModeUnavailable(cameraId: String) {
                    off()
                }
            }, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun toggle() {
        cameraId?.let { cameraManager.setTorchMode(it, _mode == Mode.Off) }
    }

    private fun on() {
        _mode = Mode.On

        viewBinding.frameLayoutHighlight.scale(1.0F)
        viewBinding.imageViewHighlight.setColorFilter(
            ContextCompat.getColor(context, R.color.yellow_A_200),
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun off() {
        _mode = Mode.Off

        viewBinding.frameLayoutHighlight.scale(0.5F, alpha = 0F)
        viewBinding.imageViewHighlight.setColorFilter(
            ContextCompat.getColor(context, R.color.white),
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun cameraId(): String? {
        cameraManager.cameraIdList.forEach { cameraId ->
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
            val flashInfoAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: return null
            val lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ?: return null

            if (flashInfoAvailable && lensFacing == CameraCharacteristics.LENS_FACING_BACK)
                return cameraId
        }

        return null
    }
}