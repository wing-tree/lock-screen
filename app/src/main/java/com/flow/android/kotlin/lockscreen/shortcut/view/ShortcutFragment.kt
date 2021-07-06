package com.flow.android.kotlin.lockscreen.shortcut.view

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentShortcutBinding
import com.flow.android.kotlin.lockscreen.devicecredential.DeviceCredentialHelper
import com.flow.android.kotlin.lockscreen.devicecredential.RequireDeviceCredential
import com.flow.android.kotlin.lockscreen.shortcut.adapter.ShortcutAdapter
import com.flow.android.kotlin.lockscreen.shortcut.adapter.ItemTouchCallback
import com.flow.android.kotlin.lockscreen.shortcut.model.ShortcutModel
import com.flow.android.kotlin.lockscreen.util.BLANK
import timber.log.Timber

class ShortcutFragment: BaseMainFragment<FragmentShortcutBinding>(), RequireDeviceCredential<ShortcutFragment.Value> {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentShortcutBinding {
        return FragmentShortcutBinding.inflate(inflater, container, false)
    }

    private val shortcutAdapter = ShortcutAdapter().apply {
        setListener(object : ShortcutAdapter.Listener {
            override fun onItemClick(item: ShortcutModel) {
                if (DeviceCredentialHelper.requireUnlock(requireContext()))
                    confirmDeviceCredential(Value(true, item.packageName))
                else
                    launchApplication(item.packageName)
            }

            override fun onItemLongClick(view: View, item: ShortcutModel): Boolean {
                return showPopupMenu(view, item)
            }
        })
    }

    private val itemTouchHelper = ItemTouchHelper(ItemTouchCallback(shortcutAdapter) {
        popupMenu?.dismiss()
    })

    private var popupMenu: PopupMenu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        viewBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = shortcutAdapter
        }

        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)
        registerObservers()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.appCompatImageView.setOnClickListener {
            if (DeviceCredentialHelper.requireUnlock(requireContext())) {
                confirmDeviceCredential(Value(false))
            } else {
                AllShortcutBottomSheetDialogFragment().also {
                    it.show(requireActivity().supportFragmentManager, it.tag)
                }
            }
        }
    }

    private fun registerObservers() {
        mainViewModel.shortcuts.observe(viewLifecycleOwner, {
            shortcutAdapter.submitList(it)
        })
    }

    private fun launchApplication(packageName: String) {
        var intent: Intent? = null

        try {
            intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
        } catch (ignored: Exception) {
            Timber.e(ignored)
        }

        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(it)
        }
    }

    override fun confirmDeviceCredential(value: Value) {
        val shortcutClicked = value.shortcutClicked
        val packageName = value.packageName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DeviceCredentialHelper.confirmDeviceCredential(requireActivity(), object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    super.onDismissSucceeded()

                    if (shortcutClicked)
                        launchApplication(packageName)
                    else {
                        AllShortcutBottomSheetDialogFragment().also {
                            it.show(requireActivity().supportFragmentManager, it.tag)
                        }
                    }
                }
            })
        } else {
            DeviceCredentialHelper.confirmDeviceCredential(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            DeviceCredentialHelper.RequestCode.ConfirmDeviceCredential -> showToast("fucking man") // todo check.
        }
    }

    private fun showPopupMenu(view: View, shortcut: ShortcutModel): Boolean {
        popupMenu = PopupMenu(requireContext(), view)
        popupMenu?.inflate(R.menu.shortcut)
        popupMenu?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    mainViewModel.deleteShortcut(shortcut.toEntity()) { showToast("removed!.") }
                    true
                }
                else -> false
            }
        }

        popupMenu?.show()
        return true
    }

    data class Value(val shortcutClicked : Boolean, val packageName: String = BLANK)
}