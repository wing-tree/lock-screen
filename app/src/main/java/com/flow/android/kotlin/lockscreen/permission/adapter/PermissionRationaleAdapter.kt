package com.flow.android.kotlin.lockscreen.permission.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.databinding.PermissionRationaleBinding
import com.flow.android.kotlin.lockscreen.permission.view.PermissionRationale

class PermissionRationaleAdapter(private val items: List<PermissionRationale>): RecyclerView.Adapter<PermissionRationaleAdapter.ViewHolder>() {
    private var inflater: LayoutInflater? = null

    class ViewHolder private constructor(private val binding: PermissionRationaleBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PermissionRationale) {
            binding.imageViewIcon.setImageResource(item.icon)
            binding.textViewPermission.text = item.permissionName
            binding.textViewRationale.text = item.rationale

            binding.imageViewKeyboardArrowUp.setOnClickListener {
                if (binding.textViewRationale.visibility == View.GONE) {

                } else {

                }
            }
        }

        companion object {
            fun from(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
                return ViewHolder(PermissionRationaleBinding.inflate(inflater, parent, false))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = this.inflater ?: LayoutInflater.from(parent.context)

        this.inflater = inflater

        return ViewHolder.from(inflater, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()
}