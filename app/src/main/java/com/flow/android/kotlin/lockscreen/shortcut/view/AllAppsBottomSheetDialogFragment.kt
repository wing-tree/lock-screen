package com.flow.android.kotlin.lockscreen.shortcut.view

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.flow.android.kotlin.lockscreen.databinding.FragmentAllAppsBottomSheetDialogBinding
import com.flow.android.kotlin.lockscreen.shortcut.adapter.AppAdapter
import com.flow.android.kotlin.lockscreen.shortcut.datamodel.App
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AllAppsBottomSheetDialogFragment: BottomSheetDialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private val appAdapter = AppAdapter { viewModel.addShortcut(it) { app ->
        removeApp(app)
    } }
    private val batchSize = 8
    private var viewBinding: FragmentAllAppsBottomSheetDialogBinding? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentAllAppsBottomSheetDialogBinding.inflate(
                inflater,
                container,
                false
        )

        viewBinding?.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = appAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            addApps()
        }

        return viewBinding?.root
    }

    private suspend fun addApps() {
        withContext(Dispatchers.IO) {
            val apps = arrayListOf<App>()
            val packageNames = viewModel.shortcuts()?.map { it.packageName } ?: emptyList()
            var count = 0

            try {
                val packageManager = requireActivity().packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

                val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
                for (resolveInfo in resolveInfoList) {
                    val icon = resolveInfo.activityInfo.loadIcon(packageManager)
                    val label = resolveInfo.loadLabel(packageManager).toString()
                    val packageName = resolveInfo.activityInfo.packageName

                    if (isDetached)
                        return@withContext

                    if (packageNames.contains(packageName))
                        continue
                    else if (packageName == requireContext().packageName)
                        continue

                    apps.add(
                            App(
                                    icon = icon,
                                    label = label,
                                    packageName = packageName
                            )
                    )

                    if (count >= batchSize) {
                        withContext(Dispatchers.Main) {
                            appAdapter.addAll(apps)
                            apps.clear()
                        }

                        count = 0
                    } else
                        ++count
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.e(e)
            } catch (e: IllegalStateException) {
                Timber.e(e)
            }

            withContext(Dispatchers.Main) {
                appAdapter.addAll(apps.toList())
            }
        }
    }

    private fun removeApp(app: App) {
        appAdapter.remove(app)
    }
}