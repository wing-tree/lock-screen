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
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel
import com.flow.android.kotlin.lockscreen.shortcut.adapter.ShortcutAdapter
import com.flow.android.kotlin.lockscreen.shortcut.model.Model
import com.flow.android.kotlin.lockscreen.util.hide
import com.flow.android.kotlin.lockscreen.util.show
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import timber.log.Timber

class AllShortcutBottomSheetDialogFragment: BottomSheetDialogFragment() {
    private val mainViewModel by activityViewModels<MainViewModel>()

    private var viewBinding: FragmentAllShortcutBottomSheetDialogBinding? = null
    private val viewModel by lazy { mainViewModel.shortcutViewModel }

    private val batchSize = 16
    private val job = Job()
    private val adapter = ShortcutAdapter().apply {
        setListener(object : ShortcutAdapter.Listener {
            override fun onItemClick(item: Model.Shortcut) {
                viewModel.insert(item.apply {
                    priority = System.currentTimeMillis()
                }.toEntity()) {
                    remove(item)
                }
            }

            override fun onItemLongClick(view: View, item: Model.Shortcut): Boolean = false
        })
    }

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
            adapter = this@AllShortcutBottomSheetDialogFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 4)
            setHasFixedSize(true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            addAll()
        }

        return viewBinding?.root
    }

    override fun onDestroyView() {
        job.cancel()
        viewBinding = null
        super.onDestroyView()
    }

    private suspend fun addAll() {
        withContext(Dispatchers.IO + job) {
            val shortcuts = arrayListOf<Model.Shortcut>()
            val packageNames = viewModel.getAll().map { it.packageName }
            var count = 0

            withContext(Dispatchers.Main) {
                viewBinding?.progressBar?.show()
            }

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

                    if (isAdded.not() || isDetached)
                        return@withContext

                    if (packageNames.contains(packageName))
                        continue
                    else if (packageName == requireContext().packageName)
                        continue

                    shortcuts.add(
                            Model.Shortcut(
                                    icon = icon,
                                    label = label,
                                    packageName = packageName,
                                    priority = 0L
                            )
                    )

                    if (count >= batchSize.dec()) {
                        withContext(Dispatchers.Main) {
                            viewBinding?.progressBar?.hide()
                            adapter.addAll(shortcuts)
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
            } catch (e: CancellationException) {
                Timber.e(e)
            }

            withContext(Dispatchers.Main) {
                adapter.addAll(shortcuts)
            }
        }
    }
}