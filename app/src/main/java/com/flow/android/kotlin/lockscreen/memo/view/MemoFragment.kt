package com.flow.android.kotlin.lockscreen.memo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.base.BaseFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoBinding
import com.flow.android.kotlin.lockscreen.memo.adapter.ItemTouchCallback
import com.flow.android.kotlin.lockscreen.memo.adapter.MemoAdapter

class MemoFragment: BaseFragment<FragmentMemoBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentMemoBinding {
        return FragmentMemoBinding.inflate(inflater, container, false)
    }

    private val memoAdapter: MemoAdapter = MemoAdapter (arrayListOf(), { memo ->
        MemoDetailDialogFragment.getInstance(memo.deepCopy()).also {
            it.show(requireActivity().supportFragmentManager, it.tag)
        }
    }, {
        itemTouchHelper.startDrag(it)
    })

    private val itemTouchHelper = ItemTouchHelper(ItemTouchCallback(memoAdapter))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        viewBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memoAdapter
        }

        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)
        initializeLiveData()

        return view
    }

    private fun initializeLiveData() {
        viewModel.memoChanged.observe(viewLifecycleOwner, {
            // update adapter.
        })

        viewModel.memos.observe(viewLifecycleOwner, {
            memoAdapter.addAll(it)
        })
    }
}