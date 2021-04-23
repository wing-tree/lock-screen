package com.flow.android.kotlin.lockscreen.memo.adapter

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.databinding.MemoBinding
import com.flow.android.kotlin.lockscreen.memo.entity.Memo

class MemoAdapter: ListAdapter<Memo, MemoAdapter.ViewHolder> {

    class ViewHolder(binding: MemoBinding) : RecyclerView.ViewHolder(binding.root)
}