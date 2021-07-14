package com.flow.android.kotlin.lockscreen.util.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.flow.android.kotlin.lockscreen.base.BaseDialogFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentConfirmationDialogBinding
import com.flow.android.kotlin.lockscreen.util.BLANK
import com.flow.android.kotlin.lockscreen.util.hide

class ConfirmationDialogFragment : BaseDialogFragment<FragmentConfirmationDialogBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentConfirmationDialogBinding {
        return FragmentConfirmationDialogBinding.inflate(inflater, container, false)
    }

    private var title = BLANK
    private var message = BLANK
    private var negativeButtonText = BLANK
    private var onNegativeButtonClick: ((DialogFragment) -> Unit)? = null
    private var positiveButtonText = BLANK
    private var onPositiveButtonClick: ((DialogFragment) -> Unit)? = null

    fun setTitle(title: String) {
        this.title = title
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun setNegativeButton(text: String, onClick: (DialogFragment) -> Unit) {
        this.negativeButtonText = text
        this.onNegativeButtonClick = onClick
    }

    fun setPositiveButton(text: String, onClick: (DialogFragment) -> Unit) {
        this.positiveButtonText = text
        this.onPositiveButtonClick = onClick
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        if (title.isBlank())
            viewBinding.textViewTitle.hide()
        else
            viewBinding.textViewTitle.text = title

        if (message.isBlank())
            viewBinding.textViewMessage.hide()
        else
            viewBinding.textViewMessage.text = message

        viewBinding.materialButtonCancel.text = negativeButtonText
        viewBinding.materialButtonCancel.setOnClickListener {
            onNegativeButtonClick?.invoke(this@ConfirmationDialogFragment)
        }

        viewBinding.materialButtonConfirm.text = positiveButtonText
        viewBinding.materialButtonConfirm.setOnClickListener {
            onPositiveButtonClick?.invoke(this@ConfirmationDialogFragment)
        }

        return view
    }
}