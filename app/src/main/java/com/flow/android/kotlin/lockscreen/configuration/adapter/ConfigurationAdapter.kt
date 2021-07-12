package com.flow.android.kotlin.lockscreen.configuration.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.databinding.*
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper
import com.flow.android.kotlin.lockscreen.util.collapse
import com.flow.android.kotlin.lockscreen.util.expand
import java.security.InvalidParameterException

class ConfigurationAdapter(private val currentList: ArrayList<AdapterItem>, private val isEnabledDividers: Boolean = true): RecyclerView.Adapter<ConfigurationAdapter.ViewHolder>() {
    private var recyclerView: RecyclerView? = null

    init {
        if (isEnabledDividers) {
            val arrayList = arrayListOf<AdapterItem>()

            currentList.forEach {
                arrayList.add(it)
                arrayList.add(AdapterItem.DividerItem())
            }

            currentList.clear()
            currentList.addAll(arrayList)
        }
    }

    class ViewHolder(private val viewBinding: ViewBinding): RecyclerView.ViewHolder(viewBinding.root) {
        private val duration = 300L

        fun bind(adapterItem: AdapterItem, viewType: Int) {
            when(viewType) {
                ViewType.Divider -> {
                    // pass
                }
                ViewType.Item -> {
                    viewBinding as PreferenceBinding
                    val item = adapterItem as AdapterItem.Item

                    viewBinding.imageViewIcon.setImageDrawable(item.drawable)
                    viewBinding.textViewSummary.text = item.description
                    viewBinding.textViewTitle.text = item.title

                    viewBinding.root.setOnClickListener {
                        item.onClick.invoke(viewBinding, item)
                    }
                }
                ViewType.List -> {
                    viewBinding as MultiSelectListPreferenceBinding

                    if (adapterItem is AdapterItem.ListItem) {
                        viewBinding.recyclerView.apply {
                            adapter = adapterItem.adapter
                            layoutManager = LinearLayoutManagerWrapper(viewBinding.root.context)
                            setHasFixedSize(true)

                            addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                                    if (e.action == MotionEvent.ACTION_MOVE)
                                        rv.parent.requestDisallowInterceptTouchEvent(true)

                                    return false
                                }

                                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                            })
                        }

                        viewBinding.textTitle.text = adapterItem.title

                        viewBinding.root.setOnClickListener {
                            adapterItem.onClick.invoke(viewBinding, adapterItem)
                        }
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

                    viewBinding.switchMaterial.setOnCheckedChangeListener { _, isChecked ->
                        switchItem.onCheckedChange(isChecked)
                    }
                }
            }
        }

        fun hide() {
            viewBinding.root.collapse(duration, 0)
        }

        fun show() {
            viewBinding.root.expand(duration)
        }

        fun updateSummary(summary: String) {
            if (viewBinding is PreferenceBinding)
                viewBinding.textViewSummary.text = summary
        }

        companion object {
            fun from(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewHolder {
                val viewBinding = when(viewType) {
                    ViewType.Divider -> DividerItemBinding.inflate(layoutInflater, parent, false)
                    ViewType.Item -> PreferenceBinding.inflate(layoutInflater, parent, false)
                    ViewType.List -> MultiSelectListPreferenceBinding.inflate(layoutInflater, parent, false)
                    ViewType.Subtitle -> SubtitleItemBinding.inflate(layoutInflater, parent, false)
                    ViewType.Switch -> SwitchItemBinding.inflate(layoutInflater, parent, false)
                    else -> throw InvalidParameterException("Invalid viewType")
                }

                return ViewHolder(viewBinding)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return ViewHolder.from(layoutInflater, parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], getItemViewType(position))
    }

    override fun getItemCount(): Int = currentList.count()

    override fun getItemViewType(position: Int): Int {
        return when(currentList[position]) {
            is AdapterItem.DividerItem -> ViewType.Divider
            is AdapterItem.Item -> ViewType.Item
            is AdapterItem.ListItem -> ViewType.List
            is AdapterItem.SubtitleItem -> ViewType.Subtitle
            is AdapterItem.SwitchItem -> ViewType.Switch
        }
    }

    fun hideItem(id: Long) {
        val item = currentList.find { it.id == id } ?: return
        val index = currentList.indexOf(item)
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(index)

        viewHolder?.let {
            if (it is ViewHolder) {
                it.hide()
            }
        }
    }

    fun showItem(id: Long) {
        val item = currentList.find { it.id == id } ?: return
        val index = currentList.indexOf(item)
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(index)

        viewHolder?.let {
            if (it is ViewHolder) {
                it.show()
            }
        }
    }

    fun updateDescription(id: Long, description: String) {
        val item = currentList.find { it.id == id } ?: return
        val index = currentList.indexOf(item)

        if (item is AdapterItem.Item) {
            val viewHolder = recyclerView?.findViewHolderForAdapterPosition(index)

            if (viewHolder is ViewHolder) {
                viewHolder.updateSummary(description)
            }
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
    abstract var isEnabled: Boolean

    data class DividerItem(
        override val id: Long = 0L,
        override var isEnabled: Boolean = true
    ): AdapterItem()

    data class Item(
            override val id: Long = 0L,
            override var isEnabled: Boolean = true,
            val description: String,
            val drawable: Drawable?,
            val onClick: (PreferenceBinding, Item) -> Unit,
            val title: String
    ): AdapterItem()

    data class ListItem(
            override val id: Long = 0L,
            override var isEnabled: Boolean = true,
            val adapter: RecyclerView.Adapter<*>,
            val drawable: Drawable?,
            val onClick: (MultiSelectListPreferenceBinding, ListItem) -> Unit,
            val title: String,
            var isExpanded: Boolean = false
    ): AdapterItem()

    data class SubtitleItem(
            override val id: Long = 0L,
            override var isEnabled: Boolean = true,
            val subtitle: String
    ): AdapterItem()

    data class SwitchItem(
            override val id: Long = 0L,
            override var isEnabled: Boolean = true,
            val drawable: Drawable?,
            val isChecked: Boolean,
            val onCheckedChange: (isChecked: Boolean) -> Unit,
            val title: String
    ): AdapterItem()
}