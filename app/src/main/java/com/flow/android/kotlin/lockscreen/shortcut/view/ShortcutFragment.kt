package com.flow.android.kotlin.lockscreen.shortcut.view

import android.app.KeyguardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.base.DataChangedState
import com.flow.android.kotlin.lockscreen.databinding.FragmentShortcutBinding
import com.flow.android.kotlin.lockscreen.databinding.PopupWindowBinding
import com.flow.android.kotlin.lockscreen.devicecredential.DeviceCredential
import com.flow.android.kotlin.lockscreen.devicecredential.RequireDeviceCredential
import com.flow.android.kotlin.lockscreen.shortcut.adapter.ItemTouchCallback
import com.flow.android.kotlin.lockscreen.shortcut.adapter.ShortcutAdapter
import com.flow.android.kotlin.lockscreen.shortcut.model.Model
import com.flow.android.kotlin.lockscreen.shortcut.viewmodel.ShortcutViewModel
import com.flow.android.kotlin.lockscreen.util.BLANK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ShortcutFragment: BaseMainFragment<FragmentShortcutBinding>(), RequireDeviceCredential<ShortcutFragment.Value> {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentShortcutBinding {
        return FragmentShortcutBinding.inflate(inflater, container, false)
    }

    private val viewModel by activityViewModels<ShortcutViewModel>()

    private val activityResultLauncherMap = mapOf(
        DeviceCredential.Key.ConfirmDeviceCredential to registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { Timber.d(tag) }
    )

    private val adapter = ShortcutAdapter().apply {
        setListener(object : ShortcutAdapter.Listener {
            override fun onItemClick(item: Model.Shortcut) {
                if (DeviceCredential.requireUnlock(requireContext()))
                    confirmDeviceCredential(Value(true, item.packageName))
                else
                    launchApplication(item.packageName)
            }

            override fun onItemLongClick(view: View, item: Model.Shortcut): Boolean {
                return showPopupWindow(view, item)
            }
        })
    }

    private val itemTouchHelper = ItemTouchHelper(ItemTouchCallback(adapter, {
        popupWindow?.dismiss()
    }) {
        viewModel.updateAll(adapter.currentList())
    })

    private var popupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        viewBinding.recyclerView.apply {
            adapter = this@ShortcutFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 4)
            scheduleLayoutAnimation()
            setHasFixedSize(true)
        }

        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)
        initializeData()
        registerLifecycleObservers()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.imageView.setOnClickListener {
            if (DeviceCredential.requireUnlock(requireContext())) {
                confirmDeviceCredential(Value(false))
            } else {
                AllShortcutBottomSheetDialogFragment().also {
                    it.show(requireActivity().supportFragmentManager, it.tag)
                }
            }
        }
    }

    private fun initializeData() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAll().run {
                withContext(Dispatchers.Main) {
                    adapter.addAll(this@run)
                }
            }
        }
    }

    private fun registerLifecycleObservers() {
        viewModel.dataChanged.observe(viewLifecycleOwner, {
            when (it.state) {
                DataChangedState.Deleted -> {
                    adapter.remove(it.data)
                }
                DataChangedState.Inserted -> {
                    adapter.add(it.data)
                }
                DataChangedState.Updated -> {
                    adapter.update(it.data)
                }
            }
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
            DeviceCredential.confirmDeviceCredential(
                requireActivity(),
                object : KeyguardManager.KeyguardDismissCallback() {
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
            activityResultLauncherMap[DeviceCredential.Key.ConfirmDeviceCredential]?.let {
                DeviceCredential.confirmDeviceCredential(this, it)
            }
        }
    }

    private fun showPopupWindow(view: View, shortcut: Model.Shortcut): Boolean {
        val popupWindowBinding = PopupWindowBinding.bind(
            layoutInflater.inflate(R.layout.popup_window, viewBinding.root, false)
        )

        layoutInflater.inflate(R.layout.popup_window, viewBinding.root, false).also {
            popupWindow = PopupWindow(
                popupWindowBinding.root,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow?.let{ popupWindow ->
                popupWindowBinding.root.setOnClickListener {
                    viewModel.delete(shortcut.toEntity())
                    popupWindow.dismiss()
                }

                popupWindow.elevation = resources.getDimensionPixelSize(R.dimen.elevation_8dp).toFloat()

                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                popupWindow.showAsDropDown(view)
            }
        }

        return true
    }

    data class Value(val shortcutClicked: Boolean, val packageName: String = BLANK)
}