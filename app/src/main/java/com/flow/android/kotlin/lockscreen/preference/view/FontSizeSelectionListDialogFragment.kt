package com.flow.android.kotlin.lockscreen.preference.view

import android.util.TypedValue
import androidx.fragment.app.DialogFragment
import com.flow.android.kotlin.lockscreen.base.SingleChoiceListDialogFragment
import com.flow.android.kotlin.lockscreen.databinding.SingleChoiceListItemBinding

class FontSizeSelectionListDialogFragment(
    items: Array<Float>,
    private val onClick: (DialogFragment, Float) -> Unit
) : SingleChoiceListDialogFragment<Float>(items) {

    override fun bind(viewBinding: SingleChoiceListItemBinding, item: Float) {
        val text = "${item}dp"
        viewBinding.root.text = text
        viewBinding.root.setTextSize(TypedValue.COMPLEX_UNIT_DIP, item)
        viewBinding.root.setOnClickListener {
            onClick(this, item)
        }
    }
}