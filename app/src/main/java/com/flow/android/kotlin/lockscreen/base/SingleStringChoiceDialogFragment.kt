package com.flow.android.kotlin.lockscreen.base

import androidx.fragment.app.DialogFragment
import com.flow.android.kotlin.lockscreen.databinding.SingleChoiceListItemBinding

class SingleStringChoiceDialogFragment(
        singleChoiceItems: Array<String>,
        private val onItemClick: (DialogFragment, String) -> Unit
) : SingleChoiceDialogFragment<String>(singleChoiceItems) {
    override fun bind(viewBinding: SingleChoiceListItemBinding, item: String) {
        viewBinding.root.text = item
        viewBinding.root.setOnClickListener {
            onItemClick(this, item)
        }
    }
}