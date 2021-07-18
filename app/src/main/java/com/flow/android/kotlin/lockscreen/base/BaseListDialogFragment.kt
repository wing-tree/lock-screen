package com.flow.android.kotlin.lockscreen.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.databinding.FragmentBaseListDialogBinding
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper

abstract class BaseListDialogFragment<VB : ViewBinding, T>(
    private val items: Array<T>,
) : BaseDialogFragment<FragmentBaseListDialogBinding>() {
    abstract fun inflateItemView(inflater: LayoutInflater, container: ViewGroup): VB
    abstract fun bind(viewBinding: VB, item: T)

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentBaseListDialogBinding {
        return FragmentBaseListDialogBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        viewBinding.recyclerView.apply {
            adapter = BaseListAdapter()
            layoutManager = LinearLayoutManagerWrapper(requireContext())
        }

        return viewBinding.root
    }

    inner class BaseListAdapter: RecyclerView.Adapter<BaseListAdapter.ViewHolder>() {
        inner class ViewHolder(val viewBinding: VB): RecyclerView.ViewHolder(viewBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflateItemView(layoutInflater, parent))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            bind(holder.viewBinding, items[position])
        }

        override fun getItemCount(): Int = items.count()
    }
}