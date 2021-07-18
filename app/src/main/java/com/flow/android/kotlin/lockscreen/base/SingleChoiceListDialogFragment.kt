package com.flow.android.kotlin.lockscreen.base

import android.view.LayoutInflater
import android.view.ViewGroup
import com.flow.android.kotlin.lockscreen.databinding.SingleChoiceListItemBinding

abstract class SingleChoiceListDialogFragment<T>(items: Array<T>) :
    BaseListDialogFragment<SingleChoiceListItemBinding, T>(items) {
    override fun inflateItemView(
        inflater: LayoutInflater,
        container: ViewGroup
    ): SingleChoiceListItemBinding {
        return SingleChoiceListItemBinding.inflate(inflater, container, false)
    }
}