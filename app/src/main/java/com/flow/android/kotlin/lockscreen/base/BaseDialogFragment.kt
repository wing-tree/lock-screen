package com.flow.android.kotlin.lockscreen.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel

abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {
    private var _viewBinding: VB? = null
    protected val viewBinding: VB
        get() = _viewBinding!!

    protected val mainViewModel by activityViewModels<MainViewModel>()

    abstract fun inflate(inflater: LayoutInflater, container: ViewGroup?): VB

    @CallSuper
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _viewBinding = inflate(inflater, container)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        return viewBinding.root
    }

    override fun onDestroyView() {
        dialog?.window?.setWindowAnimations(R.style.WindowAnimation_DialogFragment)
        _viewBinding = null
        super.onDestroyView()
    }
}