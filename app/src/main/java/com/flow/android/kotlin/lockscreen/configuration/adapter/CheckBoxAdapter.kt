package com.flow.android.kotlin.lockscreen.configuration.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.databinding.CheckBoxPreferenceBinding

class CheckBoxAdapter(private val arrayList: ArrayList<CheckBoxItem>): RecyclerView.Adapter<CheckBoxAdapter.ViewHolder>() {

    fun addAll(list: List<CheckBoxItem>) {
        val positionStart = arrayList.count()
        arrayList.addAll(list)
        notifyItemRangeInserted(positionStart, arrayList.count().dec())
    }

    class ViewHolder(private val viewBinding: ViewBinding): RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: CheckBoxItem) {
            viewBinding as CheckBoxPreferenceBinding

            viewBinding.checkBox.isChecked = item.isChecked
            viewBinding.checkBox.text = item.text

            viewBinding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                item.onCheckedChange(isChecked)
            }
        }

        companion object {
            fun from(layoutInflater: LayoutInflater, parent: ViewGroup): ViewHolder {
                val viewBinding = CheckBoxPreferenceBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(viewBinding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return ViewHolder.from(layoutInflater, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrayList[position])
    }

    override fun getItemCount(): Int = arrayList.count()
}

data class CheckBoxItem(
        var isChecked: Boolean,
        val text: String,
        val onCheckedChange: (isChecked: Boolean) -> Unit
)