package com.flow.android.kotlin.lockscreen.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.preference.view.PreferenceActivity
import com.flow.android.kotlin.lockscreen.databinding.FragmentPreferenceBinding
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper

abstract class PreferenceFragment : BaseFragment<FragmentPreferenceBinding>() {
    abstract fun createPreferenceAdapter() : PreferenceAdapter
    abstract val toolbarTitleResId: Int

    protected val preferenceAdapter : PreferenceAdapter by lazy {
        createPreferenceAdapter()
    }

    private fun initializeToolbar() {
        val activity = requireActivity()

        if (activity is PreferenceActivity)
            activity.supportActionBar?.setTitle(toolbarTitleResId)
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentPreferenceBinding {
        return FragmentPreferenceBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        initializeToolbar()

        viewBinding.recyclerView.apply {
            adapter = preferenceAdapter
            layoutManager = LinearLayoutManagerWrapper(requireContext())
        }

        return viewBinding.root
    }
}