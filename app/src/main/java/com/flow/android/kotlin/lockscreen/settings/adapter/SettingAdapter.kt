package com.flow.android.kotlin.lockscreen.settings.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.databinding.*
import java.security.InvalidParameterException

class SettingAdapter(private val arrayList: ArrayList<AdapterItem>): RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    class ViewHolder(private val viewBinding: ViewBinding): RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(adapterItem: AdapterItem, viewType: Int) {
            when(viewType) {
                ViewType.Divider -> {
                    // pass
                }
                ViewType.Item -> {
                    viewBinding as ItemBinding
                    val item = adapterItem as AdapterItem.Item
                }
                ViewType.List -> {
                    viewBinding as ListItemBinding
                    val listItem = adapterItem as AdapterItem.ListItem
                }
                ViewType.Subtitle -> {
                    viewBinding as SubtitleItemBinding
                    val subtitleItem = adapterItem as AdapterItem.SubtitleItem
                }
                ViewType.Switch -> {
                    viewBinding as SwitchItemBinding
                    val switchItem = adapterItem as AdapterItem.SwitchItem
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = when(viewType) {
            ViewType.Divider -> DividerItemBinding.inflate(inflater, parent, false)
            ViewType.Item -> ItemBinding.inflate(inflater, parent, false)
            ViewType.List -> ListItemBinding.inflate(inflater, parent, false)
            ViewType.Subtitle -> SubtitleItemBinding.inflate(inflater, parent, false)
            ViewType.Switch -> SwitchItemBinding.inflate(inflater, parent, false)
            else -> throw InvalidParameterException("Invalid viewType.")
        }

        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrayList[position], getItemViewType(position))
    }

    override fun getItemCount(): Int = arrayList.count()

    override fun getItemViewType(position: Int): Int {
        return when(arrayList[position]) {
            is AdapterItem.DividerItem -> ViewType.Divider
            is AdapterItem.Item -> ViewType.Item
            is AdapterItem.ListItem -> ViewType.List
            is AdapterItem.SubtitleItem -> ViewType.Subtitle
            is AdapterItem.SwitchItem -> ViewType.Switch
        }
    }
}

object ViewType {
    const val Divider = 0
    const val Item = 1
    const val List = 2
    const val Subtitle = 3
    const val Switch = 4
}

sealed class AdapterItem {
    abstract val id: Long

    data class DividerItem(override val id: Long): AdapterItem()
    data class Item(override val id: Long): AdapterItem()
    data class ListItem(override val id: Long): AdapterItem()
    data class SubtitleItem(override val id: Long): AdapterItem()
    data class SwitchItem(override val id: Long): AdapterItem()
}