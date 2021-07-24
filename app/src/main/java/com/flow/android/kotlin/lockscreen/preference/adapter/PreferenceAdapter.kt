package com.flow.android.kotlin.lockscreen.preference.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.flow.android.kotlin.lockscreen.databinding.*
import com.flow.android.kotlin.lockscreen.util.*
import java.security.InvalidParameterException

class PreferenceAdapter(private val currentList: ArrayList<AdapterItem>): RecyclerView.Adapter<PreferenceAdapter.ViewHolder>() {
    private val applicationContext = MainApplication.instance.applicationContext
    private var recyclerView: RecyclerView? = null

    inner class ViewHolder(private val viewBinding: ViewBinding): RecyclerView.ViewHolder(viewBinding.root) {
        private val duration = 300L

        fun bind(adapterItem: AdapterItem) {
            viewBinding.root.isClickable = adapterItem.isClickable

            if (adapterItem.isVisible)
                viewBinding.root.show()
            else
                viewBinding.root.hide()

            when(viewBinding) {
                is ContentBinding -> {
                    if (adapterItem is AdapterItem.Content) {
                        adapterItem.drawable?.let {
                            viewBinding.imageViewIcon.show()
                            viewBinding.imageViewIcon.setImageDrawable(it)
                        } ?: let {
                            viewBinding.imageViewIcon.hide()
                        }

                        viewBinding.textViewSummary.text = adapterItem.summary
                        viewBinding.textViewTitle.text = adapterItem.title

                        if (adapterItem.summary.isBlank())
                            viewBinding.textViewSummary.hide()

                        viewBinding.root.setOnClickListener {
                            adapterItem.onClick?.invoke(viewBinding, adapterItem)
                        }

                        if (isAboveSpace(adapterPosition))
                            viewBinding.viewDivider.hide()
                        else
                            viewBinding.viewDivider.show()
                    }
                }
                is PreferenceBinding -> {
                    if (adapterItem is AdapterItem.Preference) {
                        adapterItem.drawable?.let {
                            viewBinding.imageViewIcon.show()
                            viewBinding.imageViewIcon.setImageDrawable(it)
                        } ?: let {
                            viewBinding.imageViewIcon.hide()
                        }

                        viewBinding.textViewSummary.text = adapterItem.summary
                        viewBinding.textViewTitle.text = adapterItem.title

                        if (adapterItem.summary.isBlank())
                            viewBinding.textViewSummary.hide()

                        viewBinding.root.setOnClickListener {
                            adapterItem.onClick.invoke(viewBinding, adapterItem)
                        }

                        if (isAboveSpace(adapterPosition))
                            viewBinding.viewDivider.hide()
                        else
                            viewBinding.viewDivider.show()
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
                            if (adapterItem.isExpanded) {
                                val constrainedHeight = applicationContext.resources.getDimensionPixelSize(R.dimen.height_256dp)
                                val heightOneLine = applicationContext.resources.getDimensionPixelSize(R.dimen.height_48dp)

                                var to = adapterItem.adapter.itemCount * heightOneLine

                                if (to > constrainedHeight)
                                    to = constrainedHeight

                                viewBinding.imageViewKeyboardArrowUp.rotate(0F, duration)
                                viewBinding.constraintLayoutEntries.expand(duration, to.inc())
                            } else {
                                viewBinding.imageViewKeyboardArrowUp.rotate(180F, duration)
                                viewBinding.constraintLayoutEntries.collapse(duration, 0)
                            }

                            adapterItem.isExpanded = adapterItem.isExpanded.not()
                            adapterItem.onClick?.invoke(viewBinding, adapterItem)
                        }
                    }
                }
                is PreferenceCategoryBinding -> {
                    if (adapterItem is AdapterItem.PreferenceCategory)
                        viewBinding.textViewCategory.text = adapterItem.category
                }
                is SpaceBinding -> {
                    // pass
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
    }

    private fun createViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = when(viewType) {
            ViewType.Content -> ContentBinding.inflate(layoutInflater, parent, false)
            ViewType.Preference -> PreferenceBinding.inflate(layoutInflater, parent, false)
            ViewType.MultiSelectListPreference -> MultiSelectListPreferenceBinding.inflate(layoutInflater, parent, false)
            ViewType.PreferenceCategory -> PreferenceCategoryBinding.inflate(layoutInflater, parent, false)
            ViewType.Space -> SpaceBinding.inflate(layoutInflater, parent, false)
            ViewType.SwitchPreference -> SwitchPreferenceBinding.inflate(layoutInflater, parent, false)
            else -> throw InvalidParameterException("Invalid viewType")
        }

        return ViewHolder(viewBinding)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(LayoutInflater.from(parent.context), parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.count()

    override fun getItemViewType(position: Int): Int {
        return when(currentList[position]) {
            is AdapterItem.Content -> ViewType.Content
            is AdapterItem.MultiSelectListPreference -> ViewType.MultiSelectListPreference
            is AdapterItem.Preference -> ViewType.Preference
            is AdapterItem.PreferenceCategory -> ViewType.PreferenceCategory
            is AdapterItem.Space -> ViewType.Space
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

    private fun isAboveSpace(position: Int): Boolean {
        return if (position < currentList.count().dec()) {
            currentList[position.inc()] is AdapterItem.Space
        } else
            false
    }
}

private object ViewType {
    const val Content = 0
    const val MultiSelectListPreference = 1
    const val Preference = 2
    const val PreferenceCategory = 3
    const val Space = 4
    const val SwitchPreference = 5
}

sealed class AdapterItem {
    abstract val id: Long
    abstract var isClickable: Boolean
    abstract var isVisible: Boolean

    data class Content(
            override val id: Long = 0L,
            override var isClickable: Boolean = true,
            override var isVisible: Boolean = true,
            val drawable: Drawable?,
            val onClick: ((ContentBinding, Content) -> Unit)? = null,
            val title: String,
            var summary: String = BLANK,
    ) : AdapterItem()

    data class Space(
            override val id: Long = 0L,
            override var isClickable: Boolean = false,
            override var isVisible: Boolean = true,
    ) : AdapterItem()

    data class Preference(
            override val id: Long = 0L,
            override var isClickable: Boolean = true,
            override var isVisible: Boolean = true,
            val summary: String,
            val drawable: Drawable?,
            val onClick: (PreferenceBinding, Preference) -> Unit,
            val title: String
    ) : AdapterItem()

    data class MultiSelectListPreference(
            override val id: Long = 0L,
            override var isClickable: Boolean = true,
            override var isVisible: Boolean = true,
            val adapter: RecyclerView.Adapter<*>,
            val drawable: Drawable?,
            val onClick: ((MultiSelectListPreferenceBinding, MultiSelectListPreference) -> Unit)? = null,
            val title: String,
            var isExpanded: Boolean = false
    ) : AdapterItem()

    data class PreferenceCategory(
            override val id: Long = 0L,
            override var isClickable: Boolean = true,
            override var isVisible: Boolean = true,
            val category: String
    ) : AdapterItem()

    data class SwitchPreference(
            override val id: Long = 0L,
            override var isClickable: Boolean = true,
            override var isVisible: Boolean = true,
            val drawable: Drawable?,
            val isChecked: Boolean,
            val onCheckedChange: (isChecked: Boolean) -> Unit,
            val title: String
    ) : AdapterItem()
}