package com.flow.android.kotlin.lockscreen.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.configuration.view.ConfigurationActivity
import com.flow.android.kotlin.lockscreen.configuration.viewmodel.ConfigurationViewModel
import com.flow.android.kotlin.lockscreen.databinding.FragmentConfigurationBinding
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper

abstract class ConfigurationFragment : BaseFragment<FragmentConfigurationBinding>() {
    abstract fun createConfigurationAdapter() : ConfigurationAdapter
    abstract val toolbarTitleResId: Int

    protected val viewModel by activityViewModels<ConfigurationViewModel>()

    protected val configurationAdapter : ConfigurationAdapter by lazy {
        createConfigurationAdapter()
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        val activity = requireActivity()

        if (activity is ConfigurationActivity) {
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.setHomeButtonEnabled(true)
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar?.setTitle(toolbarTitleResId)
            toolbar.setNavigationOnClickListener { activity.onBackPressed() }
        }
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentConfigurationBinding {
        return FragmentConfigurationBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        initializeToolbar(viewBinding.toolbar)

        viewBinding.recyclerView.apply {
            adapter = configurationAdapter
            layoutManager = LinearLayoutManagerWrapper(requireContext())
        }

        return viewBinding.root
    }
}