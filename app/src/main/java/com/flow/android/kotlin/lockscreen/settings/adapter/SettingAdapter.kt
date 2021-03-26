package com.flow.android.kotlin.lockscreen.settings.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.databinding.*
import com.flow.android.kotlin.lockscreen.util.rotate
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

                    viewBinding.image.setImageDrawable(item.drawable)
                    viewBinding.textDescription.text = item.description
                    viewBinding.textTitle.text = item.title

                    viewBinding.root.setOnClickListener {
                        item.onClick.invoke(viewBinding, item)
                    }
                }
                ViewType.List -> {
                    viewBinding as ListItemBinding
                    val listItem = adapterItem as AdapterItem.ListItem

                    viewBinding.image.setImageDrawable(listItem.drawable)
                    viewBinding.recyclerView.apply {
                        adapter = listItem.adapter
                        layoutManager = LinearLayoutManager(viewBinding.root.context)
                    }
                    viewBinding.textTitle.text = listItem.title

                    viewBinding.root.setOnClickListener {
                        listItem.onClick.invoke(viewBinding, listItem)
                    }
                }
                ViewType.Subtitle -> {
                    viewBinding as SubtitleItemBinding
                    val subtitleItem = adapterItem as AdapterItem.SubtitleItem

                    viewBinding.textSubtitle.text = subtitleItem.subtitle
                }
                ViewType.Switch -> {
                    viewBinding as SwitchItemBinding
                    val switchItem = adapterItem as AdapterItem.SwitchItem

                    viewBinding.image.setImageDrawable(switchItem.drawable)
                    viewBinding.switchMaterial.isChecked = switchItem.isChecked
                    viewBinding.textTitle.text = switchItem.title

                    viewBinding.root.setOnClickListener {
                        switchItem.onClick.invoke(viewBinding, switchItem)
                    }
                }
            }
        }

        companion object {
            fun from(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewHolder {
                val viewBinding = when(viewType) {
                    ViewType.Divider -> DividerItemBinding.inflate(layoutInflater, parent, false)
                    ViewType.Item -> ItemBinding.inflate(layoutInflater, parent, false)
                    ViewType.List -> ListItemBinding.inflate(layoutInflater, parent, false)
                    ViewType.Subtitle -> SubtitleItemBinding.inflate(layoutInflater, parent, false)
                    ViewType.Switch -> SwitchItemBinding.inflate(layoutInflater, parent, false)
                    else -> throw InvalidParameterException("Invalid viewType.")
                }

                return ViewHolder(viewBinding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return ViewHolder.from(layoutInflater, parent, viewType)
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

    data class DividerItem(override val id: Long = 0L): AdapterItem()

    data class Item(
            override val id: Long = 0L,
            val description: String,
            val drawable: Drawable?,
            val onClick: (ItemBinding, Item) -> Unit,
            val title: String
    ): AdapterItem()

    data class ListItem(
            override val id: Long = 0L,
            val adapter: RecyclerView.Adapter<*>,
            val drawable: Drawable?,
            val onClick: (ListItemBinding, ListItem) -> Unit,
            val title: String
    ): AdapterItem()

    data class SubtitleItem(
            override val id: Long = 0L,
            val subtitle: String
    ): AdapterItem()

    data class SwitchItem(
            override val id: Long = 0L,
            val drawable: Drawable?,
            val isChecked: Boolean,
            val onClick: (SwitchItemBinding, SwitchItem) -> Unit,
            val title: String
    ): AdapterItem()
}