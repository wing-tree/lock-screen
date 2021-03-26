package com.flow.android.kotlin.lockscreen.settings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.FragmentSettingsBinding
import com.flow.android.kotlin.lockscreen.databinding.ListItemBinding
import com.flow.android.kotlin.lockscreen.main.view_model.MainViewModel
import com.flow.android.kotlin.lockscreen.settings.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.settings.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.settings.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.settings.adapter.SettingAdapter

class SettingsFragment: Fragment() {

    private var viewBinding: FragmentSettingsBinding? = null
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                    MainViewModel(requireActivity().contentResolver) as T
        }).get(MainViewModel::class.java)
    }
    private val settingAdapter: SettingAdapter by lazy {
        SettingAdapter(arrayListOf(
                AdapterItem.SubtitleItem(
                        subtitle = getString(R.string.calendar)
                ),
                AdapterItem.ListItem(
                        adapter = checkBoxAdapter,
                        drawable = getDrawable(R.drawable.ic_round_calendar_today_24),
                        onClick = { listItemBinding: ListItemBinding, listItem: AdapterItem.ListItem ->

                        },
                        title = getString(R.string.calendar),
                )
        ))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentSettingsBinding.inflate(layoutInflater, container, false)

        viewBinding?.recyclerView?.apply {
            adapter = settingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        val checkBoxItems = viewModel.calendarDisplays()?.map {
            CheckBoxItem(isChecked = true, text = it.name)  // TODO: EDIT
        } ?: listOf()

        checkBoxAdapter.addAll(checkBoxItems)

        return viewBinding?.root
    }

    private fun getDrawable(@DrawableRes id: Int) = ContextCompat.getDrawable(requireContext(), id)
}