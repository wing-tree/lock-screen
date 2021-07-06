package com.flow.android.kotlin.lockscreen.memo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoBinding
import com.flow.android.kotlin.lockscreen.main.viewmodel.MemoChangedState
import com.flow.android.kotlin.lockscreen.memo.adapter.ItemTouchCallback
import com.flow.android.kotlin.lockscreen.memo.adapter.MemoAdapter
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MemoFragment: BaseMainFragment<FragmentMemoBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentMemoBinding {
        return FragmentMemoBinding.inflate(inflater, container, false)
    }

    private val compositeDisposable = CompositeDisposable()
    private val memoAdapter: MemoAdapter by lazy {
        MemoAdapter(arrayListOf(), { memo ->
            MemoDetailDialogFragment.getInstance(memo.deepCopy()).also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }, {
            itemTouchHelper.startDrag(it)
        }).apply {
            setFontSize(ConfigurationPreferences.getFontSize(requireContext()))
        }
    }

    private val itemTouchHelper by lazy { ItemTouchHelper(ItemTouchCallback(memoAdapter) {

    }) }

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
            adapter = memoAdapter
            layoutManager = LinearLayoutManagerWrapper(requireContext())
            scheduleLayoutAnimation()
        }

        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)
        registerObservers()

        return view
    }

    override fun onPause() {
        mainViewModel.updateMemos(memoAdapter.items())
        super.onPause()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun registerObservers() {
        compositeDisposable.add(
            mainViewModel.memos
            .take(1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ memoAdapter.addAll(it) }, { Timber.e(it) })
        )

        mainViewModel.memoChanged.observe(viewLifecycleOwner, {
            when(it.state) {
                MemoChangedState.Deleted -> memoAdapter.remove(it.memo)
                MemoChangedState.Inserted -> memoAdapter.add(it.memo)
                MemoChangedState.Updated -> memoAdapter.change(it.memo)
            }
        })

        mainViewModel.refreshMemos.observe(viewLifecycleOwner, {
            memoAdapter.setFontSize(ConfigurationPreferences.getFontSize(requireContext()))
            memoAdapter.notifyDataSetChanged()
        })
    }
}