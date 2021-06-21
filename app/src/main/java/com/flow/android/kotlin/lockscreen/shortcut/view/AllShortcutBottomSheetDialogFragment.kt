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
import com.flow.android.kotlin.lockscreen.databinding.FragmentAllShortcutBottomSheetDialogBinding
import com.flow.android.kotlin.lockscreen.devicecredential.DeviceCredentialHelper
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel
import com.flow.android.kotlin.lockscreen.shortcut.adapter.ShortcutAdapter
import com.flow.android.kotlin.lockscreen.shortcut.model.ShortcutModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AllShortcutBottomSheetDialogFragment: BottomSheetDialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()

    private val shortcutAdapter = ShortcutAdapter().apply {
        setListener(object : ShortcutAdapter.Listener {
            override fun onItemClick(item: ShortcutModel) {
                viewModel.addShortcut(item.apply {
                    priority = System.currentTimeMillis()
                }.toEntity()) {
                    removeShortcut(item)
                }
            }

            override fun onItemLongClick(view: View, item: ShortcutModel): Boolean = false
        })
    }

    private val batchSize = 8
    private var viewBinding: FragmentAllShortcutBottomSheetDialogBinding? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentAllShortcutBottomSheetDialogBinding.inflate(
                inflater,
                container,
                false
        )

        viewBinding?.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = shortcutAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            addShortcuts()
        }

        return viewBinding?.root
    }

    private suspend fun addShortcuts() {
        withContext(Dispatchers.IO) {
            val shortcuts = arrayListOf<ShortcutModel>()
            val packageNames = viewModel.shortcutValues.map { it.packageName }
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

                    shortcuts.add(
                            ShortcutModel(
                                    icon = icon,
                                    label = label,
                                    packageName = packageName,
                                    priority = 0L
                            )
                    )

                    if (count >= batchSize) {
                        withContext(Dispatchers.Main) {
                            shortcutAdapter.addAll(shortcuts)
                            shortcuts.clear()
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
                shortcutAdapter.addAll(shortcuts)
            }
        }
    }

    private fun removeShortcut(item: ShortcutModel) {
        shortcutAdapter.remove(item)
    }
}