package com.flow.android.kotlin.lockscreen.memo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.base.BaseFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoBinding

class MemoFragment: BaseFragment<FragmentMemoBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentMemoBinding {
        return FragmentMemoBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun initializeLiveData() {
        viewModel.memos.observe(viewLifecycleOwner, {

        })
    }
}