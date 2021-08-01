package com.flow.android.kotlin.lockscreen.appshortcut.view

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.flow.android.kotlin.lockscreen.appshortcut.adapter.AppShortcutAdapter
import com.flow.android.kotlin.lockscreen.appshortcut.listener.ItemChangedListener
import com.flow.android.kotlin.lockscreen.appshortcut.model.Model
import com.flow.android.kotlin.lockscreen.databinding.FragmentAllAppShortcutBottomSheetDialogBinding
import com.flow.android.kotlin.lockscreen.util.hide
import com.flow.android.kotlin.lockscreen.util.show
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator
import kotlinx.coroutines.*
import timber.log.Timber

class AllAppShortcutBottomSheetDialogFragment: BottomSheetDialogFragment() {
    private var viewBinding: FragmentAllAppShortcutBottomSheetDialogBinding? = null
    private var itemChangedListener: ItemChangedListener? = null

    private val batchSize = 16
    private val job = Job()
    private val adapter = AppShortcutAdapter().apply {
        setListener(object : AppShortcutAdapter.Listener {
            override fun onItemClick(item: Model.AppShortcut) {
                itemChangedListener?.onInsert(item.apply {
                    priority = System.currentTimeMillis()
                }.toEntity()) {
                    remove(item)
                }
            }

            override fun onItemLongClick(view: View, item: Model.AppShortcut): Boolean = false
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parentFragment?.let {
            if (it is ItemChangedListener)
                itemChangedListener = it
        }

        viewBinding = FragmentAllAppShortcutBottomSheetDialogBinding.inflate(
            inflater,
            container,
            false
        )

        viewBinding?.recyclerView?.apply {
            adapter = this@AllAppShortcutBottomSheetDialogFragment.adapter
            itemAnimator = FadeInUpAnimator()
            layoutManager = GridLayoutManager(requireContext(), 4)
            scheduleLayoutAnimation()
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
            val shortcuts = arrayListOf<Model.AppShortcut>()
            val packageNames = arguments?.getStringArrayList(Extra.PackageNames) ?: arrayListOf()
            var count = 0

            withContext(Dispatchers.Main) {
                viewBinding?.progressBar?.show()
            }

            try {
                val packageManager = requireActivity().packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

                val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(
                    intent,
                    0
                )
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
                        Model.AppShortcut(
                            icon = icon,
                            label = label,
                            packageName = packageName,
                            priority = 0L
                        )
                    )

                    if (count >= batchSize.dec()) {
                        withContext(Dispatchers.Main) {
                            viewBinding?.progressBar?.hide()
                            adapter.addAll(shortcuts.toList())
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

    companion object {
        private object Extra {
            private const val Prefix = "com.flow.android.kotlin.lockscreen.shortcut.view"+
                    ".AllShortcutBottomSheetDialogFragment.companion.Extra"
            const val PackageNames = "$Prefix.PackageNames"
        }

        fun newInstance(packageNames: List<String>): AllAppShortcutBottomSheetDialogFragment {
            return AllAppShortcutBottomSheetDialogFragment().apply {
                arguments = Bundle().apply { putStringArrayList(Extra.PackageNames, ArrayList(packageNames)) }
            }
        }
    }
}