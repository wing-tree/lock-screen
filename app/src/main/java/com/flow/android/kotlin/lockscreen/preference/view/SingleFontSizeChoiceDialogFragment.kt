package com.flow.android.kotlin.lockscreen.preference.view

import android.util.TypedValue
import androidx.fragment.app.DialogFragment
import com.flow.android.kotlin.lockscreen.base.SingleChoiceDialogFragment
import com.flow.android.kotlin.lockscreen.databinding.SingleChoiceListItemBinding

class SingleFontSizeChoiceDialogFragment(
    singleChoiceItems: Array<Float>,
    private val onItemClick: (DialogFragment, Float) -> Unit
) : SingleChoiceDialogFragment<Float>(singleChoiceItems) {
    override fun bind(viewBinding: SingleChoiceListItemBinding, item: Float) {
        val text = "${item}dp"
        viewBinding.root.text = text
        viewBinding.root.setTextSize(TypedValue.COMPLEX_UNIT_DIP, item)
        viewBinding.root.setOnClickListener {
            onItemClick(this, item)
        }
    }
}