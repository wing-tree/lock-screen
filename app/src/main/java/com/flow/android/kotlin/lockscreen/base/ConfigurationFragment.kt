package com.flow.android.kotlin.lockscreen.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.configuration.viewmodel.ConfigurationViewModel
import com.flow.android.kotlin.lockscreen.databinding.FragmentConfigurationBinding

abstract class ConfigurationFragment : BaseFragment<FragmentConfigurationBinding>() {
    protected val viewModel by activityViewModels<ConfigurationViewModel>()

    protected val configurationAdapter : ConfigurationAdapter by lazy {
        createConfigurationAdapter()
    }

    abstract fun createConfigurationAdapter() : ConfigurationAdapter

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentConfigurationBinding {
        return FragmentConfigurationBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        viewBinding.recyclerView.apply {
            adapter = configurationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        return viewBinding.root
    }
}