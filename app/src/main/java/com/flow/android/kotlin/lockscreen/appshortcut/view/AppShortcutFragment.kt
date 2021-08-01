package com.flow.android.kotlin.lockscreen.appshortcut.view

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.base.DataChanged
import com.flow.android.kotlin.lockscreen.base.DataChangedState
import com.flow.android.kotlin.lockscreen.databinding.PopupWindowBinding
import com.flow.android.kotlin.lockscreen.devicecredential.DeviceCredential
import com.flow.android.kotlin.lockscreen.devicecredential.RequireDeviceCredential
import com.flow.android.kotlin.lockscreen.persistence.entity.AppShortcut
import com.flow.android.kotlin.lockscreen.appshortcut.adapter.ItemTouchCallback
import com.flow.android.kotlin.lockscreen.appshortcut.adapter.AppShortcutAdapter
import com.flow.android.kotlin.lockscreen.appshortcut.listener.ItemChangedListener
import com.flow.android.kotlin.lockscreen.appshortcut.model.Model
import com.flow.android.kotlin.lockscreen.appshortcut.viewmodel.AppShortcutViewModel
import com.flow.android.kotlin.lockscreen.databinding.FragmentAppShortcutBinding
import com.flow.android.kotlin.lockscreen.util.BLANK
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppShortcutFragment: BaseMainFragment<FragmentAppShortcutBinding>(),
    RequireDeviceCredential<AppShortcutFragment.Value>, ItemChangedListener
{
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentAppShortcutBinding {
        return FragmentAppShortcutBinding.inflate(inflater, container, false)
    }

    private val viewModel by viewModels<AppShortcutViewModel>()
    private val compositeDisposable = CompositeDisposable()
    private val publishSubject = PublishSubject.create<DataChanged<Model.AppShortcut>>()

    private val activityResultLauncherMap = mapOf(
        DeviceCredential.Key.ConfirmDeviceCredential to registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { Timber.d(tag) }
    )

    private val adapter by lazy {
        AppShortcutAdapter().apply {
            setListener(object : AppShortcutAdapter.Listener {
                override fun onItemClick(item: Model.AppShortcut) {
                    if (DeviceCredential.requireUnlock(requireContext()))
                        confirmDeviceCredential(Value(true, item.packageName))
                    else
                        launchApplication(item.packageName)
                }

                override fun onItemLongClick(view: View, item: Model.AppShortcut): Boolean {
                    return showPopupWindow(view, item)
                }
            })
        }
    }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(ItemTouchCallback(adapter, {
            popupWindow?.dismiss()
        }) {
            viewModel.updateAll(adapter.currentList())
        })
    }

    private var popupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        initializeViews()
        initializeData()
        subscribeObservables()

        return viewBinding.root
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
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

    private fun initializeViews() {
        viewBinding.recyclerView.apply {
            adapter = this@AppShortcutFragment.adapter
            itemAnimator = FadeInUpAnimator()
            layoutManager = GridLayoutManager(requireContext(), 4)
            scheduleLayoutAnimation()
        }

        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

        viewBinding.imageView.setOnClickListener {
            if (DeviceCredential.requireUnlock(requireContext())) {
                confirmDeviceCredential(Value(false))
            } else
                showAllAppShortcutBottomSheetDialogFragment()
        }
    }

    private fun subscribeObservables() {
        compositeDisposable.add(publishSubject
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
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
            }) {
                Timber.e(it)
            }
        )
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
                        else
                            showAllAppShortcutBottomSheetDialogFragment()
                    }
                })
        } else {
            activityResultLauncherMap[DeviceCredential.Key.ConfirmDeviceCredential]?.let {
                DeviceCredential.confirmDeviceCredential(this, it)
            }
        }
    }

    private fun showPopupWindow(view: View, appShortcut: Model.AppShortcut): Boolean {
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
                    viewModel.delete(appShortcut.toEntity()) {
                        publishSubject.onNext(DataChanged(it, DataChangedState.Deleted))
                    }

                    popupWindow.dismiss()
                }

                popupWindow.elevation = resources.getDimensionPixelSize(R.dimen.elevation_8dp).toFloat()

                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                popupWindow.showAsDropDown(view)
            }
        }

        return true
    }

    private fun showAllAppShortcutBottomSheetDialogFragment() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                viewModel.getAll().also { packageNames ->
                    withContext(Dispatchers.Main) {
                        AllAppShortcutBottomSheetDialogFragment.newInstance(
                            packageNames.map { it.packageName }
                        ).also {
                            it.show(childFragmentManager, it.tag)
                        }
                    }
                }
            }
        }
    }

    data class Value(val shortcutClicked: Boolean, val packageName: String = BLANK)

    override fun onInsert(item: AppShortcut, onComplete: (Model.AppShortcut) -> Unit) {
        viewModel.insert(item) {
            onComplete(it)
            publishSubject.onNext(DataChanged(it, DataChangedState.Inserted))
        }
    }
}