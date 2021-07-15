package com.flow.android.kotlin.lockscreen.preference.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.databinding.*
import com.flow.android.kotlin.lockscreen.util.*
import java.security.InvalidParameterException

class PreferenceAdapter(private val currentList: ArrayList<AdapterItem>): RecyclerView.Adapter<PreferenceAdapter.ViewHolder>() {
    private var recyclerView: RecyclerView? = null

    class ViewHolder(private val viewBinding: ViewBinding): RecyclerView.ViewHolder(viewBinding.root) {
        private val duration = 300L

        fun bind(adapterItem: AdapterItem) {
            when(viewBinding) {
                is PreferenceBinding -> {
                    if (adapterItem is AdapterItem.Preference) {
                        adapterItem.drawable?.let {
                            viewBinding.imageViewIcon.show()
                            viewBinding.imageViewIcon.setImageDrawable(it)
                            viewBinding.viewPadding.show()
                        } ?: let {
                            viewBinding.imageViewIcon.hide()
                            viewBinding.viewPadding.hide()
                        }

                        viewBinding.textViewSummary.text = adapterItem.description
                        viewBinding.textViewTitle.text = adapterItem.title

                        viewBinding.root.setOnClickListener {
                            adapterItem.onClick.invoke(viewBinding, adapterItem)
                        }
                    }
                }
                is MultiSelectListPreferenceBinding -> {
                    if (adapterItem is AdapterItem.MultiSelectListPreference) {
                        viewBinding.recyclerViewEntries.apply {
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
                is SubtitleItemBinding -> {
                    if (adapterItem is AdapterItem.SubtitleItem)
                        viewBinding.textSubtitle.text = adapterItem.subtitle
                }
                is SwitchPreferenceBinding -> {
                    if (adapterItem is AdapterItem.SwitchPreference) {
                        if (adapterItem.isVisible)
                            viewBinding.root.expand(duration)
                        else
                            viewBinding.root.collapse(duration, 0)

                        viewBinding.imageViewIcon.setImageDrawable(adapterItem.drawable)
                        viewBinding.switchMaterial.isChecked = adapterItem.isChecked
                        viewBinding.textTitle.text = adapterItem.title

                        adapterItem.drawable?.let {
                            viewBinding.imageViewIcon.show()
                            viewBinding.imageViewIcon.setImageDrawable(it)
                            viewBinding.viewPadding.show()
                        } ?: let {
                            viewBinding.imageViewIcon.hide()
                            viewBinding.viewPadding.hide()
                        }

                        viewBinding.switchMaterial.setOnCheckedChangeListener { _, isChecked ->
                            adapterItem.onCheckedChange(isChecked)
                        }
                    }
                }
            }
        }

        fun updateSummary(summary: String) {
            if (viewBinding is PreferenceBinding)
                viewBinding.textViewSummary.text = summary
        }

        companion object {
            fun from(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewHolder {
                val viewBinding = when(viewType) {
                    ViewType.Preference -> PreferenceBinding.inflate(layoutInflater, parent, false)
                    ViewType.MultiSelectListPreference -> MultiSelectListPreferenceBinding.inflate(layoutInflater, parent, false)
                    ViewType.Subtitle -> SubtitleItemBinding.inflate(layoutInflater, parent, false)
                    ViewType.SwitchPreference -> SwitchPreferenceBinding.inflate(layoutInflater, parent, false)
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
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.count()

    override fun getItemViewType(position: Int): Int {
        return when(currentList[position]) {
            is AdapterItem.Preference -> ViewType.Preference
            is AdapterItem.MultiSelectListPreference -> ViewType.MultiSelectListPreference
            is AdapterItem.SubtitleItem -> ViewType.Subtitle
            is AdapterItem.SwitchPreference -> ViewType.SwitchPreference
        }
    }

    fun getItem(id: Long) = currentList.find { it.id == id }

    fun getPosition(id: Long) = currentList.indexOf(getItem(id))

    fun updateSummary(id: Long, description: String) {
        val item = currentList.find { it.id == id } ?: return
        val index = currentList.indexOf(item)

        if (item is AdapterItem.Preference) {
            val viewHolder = recyclerView?.findViewHolderForAdapterPosition(index)

            if (viewHolder is ViewHolder) {
                viewHolder.updateSummary(description)
            }
        }
    }
}

object ViewType {
    const val Preference = 0
    const val MultiSelectListPreference = 1
    const val Subtitle = 2
    const val SwitchPreference = 3
}

sealed class AdapterItem {
    abstract val id: Long
    abstract var isEnabled: Boolean
    abstract var isVisible: Boolean

    data class Preference(
            override val id: Long = 0L,
            override var isEnabled: Boolean = true,
            override var isVisible: Boolean = true,
            val description: String,
            val drawable: Drawable?,
            val onClick: (PreferenceBinding, Preference) -> Unit,
            val title: String
    ): AdapterItem()

    data class MultiSelectListPreference(
            override val id: Long = 0L,
            override var isEnabled: Boolean = true,
            override var isVisible: Boolean = true,
            val adapter: RecyclerView.Adapter<*>,
            val drawable: Drawable?,
            val onClick: (MultiSelectListPreferenceBinding, MultiSelectListPreference) -> Unit,
            val title: String,
            var isExpanded: Boolean = false
    ): AdapterItem()

    data class SubtitleItem(
            override val id: Long = 0L,
            override var isEnabled: Boolean = true,
            override var isVisible: Boolean = true,
            val subtitle: String
    ): AdapterItem()

    data class SwitchPreference(
            override val id: Long = 0L,
            override var isEnabled: Boolean = true,
            override var isVisible: Boolean = true,
            val drawable: Drawable?,
            val isChecked: Boolean,
            val onCheckedChange: (isChecked: Boolean) -> Unit,
            val title: String
    ): AdapterItem()
}