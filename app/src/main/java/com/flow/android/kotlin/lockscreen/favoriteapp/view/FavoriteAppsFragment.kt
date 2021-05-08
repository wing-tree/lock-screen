package com.flow.android.kotlin.lockscreen.favoriteapp.view

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.flow.android.kotlin.lockscreen.base.BaseFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentFavoriteAppsBinding
import com.flow.android.kotlin.lockscreen.devicecredential.DeviceCredentialHelper
import com.flow.android.kotlin.lockscreen.devicecredential.RequireDeviceCredential
import com.flow.android.kotlin.lockscreen.favoriteapp.adapter.AppAdapter
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel

class FavoriteAppsFragment: BaseFragment<FragmentFavoriteAppsBinding>(), RequireDeviceCredential {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentFavoriteAppsBinding {
        return FragmentFavoriteAppsBinding.inflate(inflater, container, false)
    }

    private val appAdapter = AppAdapter { app ->
        launchApplication(app.packageName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        viewBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = appAdapter
        }

        initializeLiveData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.appCompatImageView.setOnClickListener {
            AllAppsBottomSheetDialogFragment().also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // confirmDeviceCredential(requireContext())
    }

    private fun initializeLiveData() {
        viewModel.favoriteApps.observe(viewLifecycleOwner, {
            appAdapter.submit(it)
        })

        viewModel.colorDependingOnBackground.observe(viewLifecycleOwner, {
            viewBinding.appCompatImageView.setColorFilter(it.onViewPagerColor, PorterDuff.Mode.SRC_IN)
        })
    }

    private fun launchApplication(packageName: String) {
        var intent: Intent? = null

        try {
            intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
        } catch (ignored: Exception) {

        }

        intent?.let { startActivity(it) }
    }

    override fun confirmDeviceCredential(context: Context) {
        DeviceCredentialHelper.confirmDeviceCredential(context)
    }
}