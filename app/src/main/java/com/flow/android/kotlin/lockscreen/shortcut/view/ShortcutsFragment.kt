package com.flow.android.kotlin.lockscreen.shortcut.view

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.flow.android.kotlin.lockscreen.base.BaseFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentFavoriteAppsBinding
import com.flow.android.kotlin.lockscreen.devicecredential.DeviceCredentialHelper
import com.flow.android.kotlin.lockscreen.devicecredential.RequireDeviceCredential
import com.flow.android.kotlin.lockscreen.shortcut.adapter.ShortcutAdapter
import timber.log.Timber

class ShortcutsFragment: BaseFragment<FragmentFavoriteAppsBinding>(), RequireDeviceCredential {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentFavoriteAppsBinding {
        return FragmentFavoriteAppsBinding.inflate(inflater, container, false)
    }

    private val appAdapter = ShortcutAdapter { app ->

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
            if (DeviceCredentialHelper.requireUnlock(requireContext())) {
                confirmDeviceCredential()
            } else {
                AllAppsBottomSheetDialogFragment().also {
                    it.show(requireActivity().supportFragmentManager, it.tag)
                }
            }
        }
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
            Timber.e(ignored)
        }

        intent?.let { startActivity(it) }
    }

    override fun confirmDeviceCredential() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DeviceCredentialHelper.confirmDeviceCredential(requireActivity(), object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissError() {
                    super.onDismissError()
                }

                override fun onDismissSucceeded() {
                    super.onDismissSucceeded()
                }

                override fun onDismissCancelled() {
                    super.onDismissCancelled()
                }
            })
        } else {
            DeviceCredentialHelper.confirmDeviceCredential(this)
        }

    }
}