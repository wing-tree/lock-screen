package com.flow.android.kotlin.lockscreen.favoriteapp.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.flow.android.kotlin.lockscreen.databinding.FragmentFavoriteAppsBinding
import com.flow.android.kotlin.lockscreen.favoriteapp.adapter.AppAdapter
import com.flow.android.kotlin.lockscreen.main.view_model.MainViewModel

class FavoriteAppsFragment: Fragment() {
    private val appAdapter = AppAdapter { app ->
        launchApplication(app.packageName)
    }

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                    MainViewModel(requireActivity().application) as T
        }).get(MainViewModel::class.java)
    }

    private var viewBinding: FragmentFavoriteAppsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentFavoriteAppsBinding.inflate(inflater, container, false)

        viewBinding?.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = appAdapter
        }

        initializeLiveData()

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding?.appCompatImageView?.setOnClickListener {
            AllAppsBottomSheetDialogFragment().also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }
    }

    private fun initializeLiveData() {
        viewModel.favoriteApps.observe(viewLifecycleOwner, {
            appAdapter.submit(it)
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
}