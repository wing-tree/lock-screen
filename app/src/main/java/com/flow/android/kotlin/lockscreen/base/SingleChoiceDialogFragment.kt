package com.flow.android.kotlin.lockscreen.base

import android.view.LayoutInflater
import android.view.ViewGroup
import com.flow.android.kotlin.lockscreen.databinding.SingleChoiceListItemBinding

abstract class SingleChoiceDialogFragment<T>(singleChoiceItems: Array<T>) :
    BaseListDialogFragment<SingleChoiceListItemBinding, T>(singleChoiceItems) {
    override fun inflateItemView(
        inflater: LayoutInflater,
        container: ViewGroup
    ): SingleChoiceListItemBinding {
        return SingleChoiceListItemBinding.inflate(inflater, container, false)
    }
}