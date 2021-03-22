package com.flow.android.kotlin.lockscreen.main.torch

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class Torch(context: Context) {

    private object Mode {
        const val On = 0
        const val Off = 1
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var mode = Mode.Off

    init {
        cameraId = cameraId()
    }

    fun toggle() {
        mode = if (mode == Mode.On) {
            off()
            Mode.Off
        } else {
            on()
            Mode.On
        }
    }

    private fun on() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            cameraId?.let {
                cameraManager.setTorchMode(it, true)
            }
        }
    }

    private fun off() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            cameraId?.let {
                cameraManager.setTorchMode(it, false)
            }
        }
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