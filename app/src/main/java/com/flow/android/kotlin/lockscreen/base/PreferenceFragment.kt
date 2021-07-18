package com.flow.android.kotlin.lockscreen.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.preference.view.PreferenceActivity
import com.flow.android.kotlin.lockscreen.preference.viewmodel.PreferenceViewModel
import com.flow.android.kotlin.lockscreen.databinding.FragmentPreferenceBinding
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper

abstract class PreferenceFragment : BaseFragment<FragmentPreferenceBinding>() {
    abstract fun createPreferenceAdapter() : PreferenceAdapter
    abstract val toolbarTitleResId: Int

    protected val viewModel by activityViewModels<PreferenceViewModel>()
    protected val preferenceAdapter : PreferenceAdapter by lazy {
        createPreferenceAdapter()
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        val activity = requireActivity()

        if (activity is PreferenceActivity) {
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.setHomeButtonEnabled(true)
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar?.setTitle(toolbarTitleResId)
            toolbar.setNavigationOnClickListener { activity.onBackPressed() }
        }
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentPreferenceBinding {
        return FragmentPreferenceBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        initializeToolbar(viewBinding.toolbar)

        viewBinding.recyclerView.apply {
            adapter = preferenceAdapter
            layoutManager = LinearLayoutManagerWrapper(requireContext())
        }

        return viewBinding.root
    }
}