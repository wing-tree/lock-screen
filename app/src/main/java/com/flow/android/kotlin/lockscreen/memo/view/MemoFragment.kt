package com.flow.android.kotlin.lockscreen.memo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.base.BaseFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoBinding
import com.flow.android.kotlin.lockscreen.main.viewmodel.MemoChangedState
import com.flow.android.kotlin.lockscreen.memo.adapter.ItemTouchCallback
import com.flow.android.kotlin.lockscreen.memo.adapter.MemoAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MemoFragment: BaseFragment<FragmentMemoBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentMemoBinding {
        return FragmentMemoBinding.inflate(inflater, container, false)
    }

    private val compositeDisposable = CompositeDisposable()
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

        viewBinding.appCompatImageView.setOnClickListener {
            MemoEditingDialogFragment.getInstance(null).also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }

        viewBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memoAdapter
        }

        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)
        initializeLiveData()

        return view
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun initializeLiveData() {
        compositeDisposable.add(
            viewModel.memos
            .take(1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ memoAdapter.addAll(it) }, { Timber.e(it) })
        )

        viewModel.memoChanged.observe(viewLifecycleOwner, {
            when(it.state) {
                MemoChangedState.Deleted -> memoAdapter.remove(it.memo)
                MemoChangedState.Inserted -> memoAdapter.add(it.memo)
                MemoChangedState.Updated -> memoAdapter.change(it.memo)
            }
        })
    }
}