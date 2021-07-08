package com.flow.android.kotlin.lockscreen.memo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.base.DataChangedState
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoBinding
import com.flow.android.kotlin.lockscreen.memo.adapter.ItemTouchCallback
import com.flow.android.kotlin.lockscreen.memo.adapter.MemoAdapter
import com.flow.android.kotlin.lockscreen.memo.viewmodel.MemoViewModel
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MemoFragment: BaseMainFragment<FragmentMemoBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentMemoBinding {
        return FragmentMemoBinding.inflate(inflater, container, false)
    }

    private val viewModel by activityViewModels<MemoViewModel>()

    private val adapter: MemoAdapter by lazy {
        MemoAdapter(object : MemoAdapter.Listener {
            override fun onItemClick(item: Memo) {
                MemoDetailDialogFragment.getInstance(item.deepCopy()).also {
                    it.show(requireActivity().supportFragmentManager, it.tag)
                }
            }

            override fun onSwapIconTouch(viewHolder: MemoAdapter.ViewHolder) {
                itemTouchHelper.startDrag(viewHolder)
            }
        }).apply {
            setFontSize(ConfigurationPreferences.getFontSize(requireContext()))
        }
    }

    private val itemTouchHelper by lazy { ItemTouchHelper(ItemTouchCallback(adapter) {
        viewModel.updateAll(adapter.currentList())
    }) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        initializeViews()
        initializeData()
        registerObservers()

        return viewBinding.root
    }

    private fun initializeViews() {
        viewBinding.appCompatImageView.setOnClickListener {
            MemoEditingDialogFragment.getInstance(null).also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }

        viewBinding.recyclerView.apply {
            adapter = this@MemoFragment.adapter
            layoutManager = LinearLayoutManagerWrapper(requireContext())
            scheduleLayoutAnimation()
        }

        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)
    }

    private fun initializeData() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAll().also {
                withContext(Dispatchers.Main) {
                    adapter.addAll(it)
                }
            }
        }
    }

    private fun registerObservers() {
        mainViewModel.refreshMemos.observe(viewLifecycleOwner, {
            adapter.setFontSize(ConfigurationPreferences.getFontSize(requireContext()))
            adapter.notifyDataSetChanged()
        })

        viewModel.dataChanged.observe(viewLifecycleOwner, {
            when(it.state) {
                DataChangedState.Deleted -> adapter.remove(it.data)
                DataChangedState.Inserted -> {
                    adapter.add(it.data)
                    viewBinding.recyclerView.scrollToPosition(0)
                }
                DataChangedState.Updated -> adapter.update(it.data)
            }
        })
    }

    companion object {
        private const val PREFIX = "com.flow.android.kotlin.lockscreen.memo.view" +
                ".MemoFragment.companion"
        const val KEY_PROCESSOR = "$PREFIX.KEY_PROCESSOR"
    }
}