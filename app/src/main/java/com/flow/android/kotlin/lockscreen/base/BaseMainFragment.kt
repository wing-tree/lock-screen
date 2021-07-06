package com.flow.android.kotlin.lockscreen.base

import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel

abstract class BaseMainFragment<VB: ViewBinding>: BaseFragment<VB>() {
    protected val mainViewModel by activityViewModels<MainViewModel>()
}