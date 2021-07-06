package com.flow.android.kotlin.lockscreen.notification.adapter

import android.animation.LayoutTransition
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import androidx.transition.AutoTransition
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.*
import com.flow.android.kotlin.lockscreen.notification.model.NotificationModel
import com.flow.android.kotlin.lockscreen.util.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val context: Context) :
    ListAdapter<NotificationModel, NotificationAdapter.ViewHolder>(DiffCallback()) {
    private val duration = 300L
    private val colorDefault by lazy { ContextCompat.getColor(context, R.color.high_emphasis_light) }
    private val margin by lazy { context.resources.getDimensionPixelSize(R.dimen.extra_small_100) }
    private var recyclerView: RecyclerView? = null

    private enum class NotificationStyle(val value: String) {
        BigPicture(Notification.BigPictureStyle::class.java.name),
        BigText(Notification.BigTextStyle::class.java.name),
        Inbox(Notification.InboxStyle::class.java.name),
        Media(Notification.MediaStyle::class.java.name),
        Messaging(
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    Notification.MessagingStyle::class.java.name
                else
                    BLANK
        )
    }

    private enum class ViewType(val value: Int) {
        BigText(0),
        BigPicture(1),
        Inbox(2),
        Media(3),
        Messaging(4),
        Base(5),
        Group(6)
    }

    private val layoutInflater by lazy { LayoutInflater.from(context) }
    private val simpleDateFormat by lazy {
        SimpleDateFormat(context.getString(R.string.format_time_000), Locale.getDefault())
    }

    inner class ViewHolder(private val viewBinding: ViewBinding): RecyclerView.ViewHolder(
            viewBinding.root
    ) {
        fun root() = viewBinding.root

        private fun addActions(container: LinearLayout, actions: Array<Notification.Action>?, @ColorInt color: Int) {
            container.removeAllViews()

            if (actions.isNullOrEmpty())
                container.hide()
            else {
                container.show()

                for (action in actions) {
                    val textView = TextView(container.context).apply {
                        gravity = Gravity.CENTER
                        setTextColor(color)
                        setTypeface(typeface, Typeface.BOLD)

                        setOnClickListener {
                            action.actionIntent.send()
                        }

                        this.text = action.title

                        with(TypedValue()) {
                            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
                            setBackgroundResource(this.resourceId)
                        }
                    }

                    textView.layoutParams = TableLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1F
                    )

                    container.addView(textView)
                }
            }
        }

        fun bind(item: NotificationModel) {
            val notification = item.notification
            val actions = notification.actions
            val extras = notification.extras
            val text = extras.getCharSequence(Notification.EXTRA_TEXT) ?: BLANK
            var color = notification.color

            val badgeIconType = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                notification.badgeIconType
            else
                0 // Notification.BADGE_ICON_NONE

            if (color == 0)
                color = colorDefault

            when(viewBinding) {
                is NotificationBigPictureBinding -> {
                    val header = viewBinding.notificationHeader
                    val body = viewBinding.notificationBody
                    val picture = extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)

                    picture?.let {
                        Glide.with(context)
                                .load(picture)
                                .centerCrop()
                                .into(viewBinding.imageViewPicture)
                    }

                    addActions(viewBinding.linearLayoutAction, notification.actions, color)

                    if (item.expanded) {
                        val to = viewBinding.linearLayoutFooter.measuredHeight(header.root)

                        header.imageViewExpand.rotate(180F, duration)
                        viewBinding.viewMarginTop.expand(duration, margin)
                        viewBinding.linearLayoutFooter.expand(duration, to)
                        viewBinding.viewMarginBottom.expand(duration, margin)
                    } else {
                        header.imageViewExpand.rotate(0F, duration)
                        viewBinding.viewMarginTop.collapse(duration, 0)
                        viewBinding.linearLayoutFooter.collapse(duration, 0)
                        viewBinding.viewMarginBottom.collapse(duration, 0)
                    }

                    bindHeader(header, item) {
                        item.expanded = item.expanded.not()

                        notifyItemChanged(adapterPosition)
                    }

                    bindBody(body, item)
                }
                is NotificationBigTextBinding -> {
                    val header = viewBinding.notificationHeader
                    val body = viewBinding.notificationBody
                    val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: text

                    addActions(viewBinding.linearLayoutAction, notification.actions, color)

                    if (item.expanded) {
                        body.textViewText.post {
                            var ellipsisStart = body.textViewText.layout.getEllipsisStart(0)

                            if (bigText.length > ellipsisStart) {
                                if (bigText[ellipsisStart].isWhitespace().not()) {
                                    val subSequence = bigText.subSequence(0, ellipsisStart)
                                    val index = subSequence.indexOfLast { it.isWhitespace() }

                                    if (index != -1)
                                        ellipsisStart = index.inc()
                                }
                            }

                            if (ellipsisStart > 0) {
                                body.textViewText.text = bigText.subSequence(0, ellipsisStart)
                                viewBinding.textViewBigText.text = bigText.subSequence(ellipsisStart, bigText.length)
                            }
                        }
                    }

                    if (item.expanded) {
                        val to = viewBinding.linearLayoutFooter.measuredHeight(header.root)

                        header.imageViewExpand.rotate(180F, duration)
                        viewBinding.viewMarginTop.expand(duration, margin)
                        viewBinding.linearLayoutFooter.expand(duration, to)
                        viewBinding.viewMarginBottom.expand(duration, margin)
                    } else {
                        header.imageViewExpand.rotate(0F, duration)
                        viewBinding.viewMarginTop.collapse(duration, 0)
                        viewBinding.linearLayoutFooter.collapse(duration, 0) {
                            body.textViewText.text = text

                            body.textViewText.post {

                                var ellipsisStart = body.textViewText.layout?.getEllipsisStart(0) ?: 0

                                if (bigText.length > ellipsisStart) {
                                    if (bigText[ellipsisStart].isWhitespace().not()) {
                                        val subSequence = bigText.subSequence(0, ellipsisStart)
                                        val index = subSequence.indexOfLast { it.isWhitespace() }

                                        if (index != -1)
                                            ellipsisStart = index.inc()
                                    }
                                }

                                if (ellipsisStart > 0)
                                    viewBinding.textViewBigText.text = bigText.subSequence(ellipsisStart, bigText.length)
                            }
                        }
                        viewBinding.viewMarginBottom.collapse(duration, 0)
                    }

                    bindHeader(header, item) {
                        item.expanded = item.expanded.not()

                        notifyItemChanged(adapterPosition)
                    }

                    bindBody(body, item)
                }
                is NotificationInboxBinding -> {
                    bindHeader(viewBinding.notificationHeader, item) {

                    }
                    bindBody(viewBinding.notificationBody, item)
                }
                is NotificationMediaBinding -> {
                    bindHeader(viewBinding.notificationHeader, item) {

                    }
                    bindBody(viewBinding.notificationBody, item)
                }
                is NotificationMessagingBinding -> {
                    bindHeader(viewBinding.notificationHeader, item) {

                    }
                    bindBody(viewBinding.notificationBody, item)
                }
                is NotificationBaseBinding -> {
                    val header = viewBinding.notificationHeader
                    val body = viewBinding.notificationBody

                    if (actions.isNullOrEmpty())
                        header.imageViewExpand.hide()
                    else {
                        header.imageViewExpand.show()
                        addActions(viewBinding.linearLayoutAction, notification.actions, color)
                    }

                    if (item.expanded) {
                        val to = viewBinding.linearLayoutFooter.measuredHeight(header.root)

                        header.imageViewExpand.rotate(180F, duration)
                        viewBinding.viewMarginTop.expand(duration, margin)
                        viewBinding.linearLayoutFooter.expand(duration, to)
                        viewBinding.viewMarginBottom.expand(duration, margin)
                    } else {
                        header.imageViewExpand.rotate(0F, duration)
                        viewBinding.viewMarginTop.collapse(duration, 0)
                        viewBinding.linearLayoutFooter.collapse(duration, 0)
                        viewBinding.viewMarginBottom.collapse(duration, 0)
                    }

                    bindHeader(viewBinding.notificationHeader, item) {
                        item.expanded = item.expanded.not()

                        notifyItemChanged(adapterPosition)
                    }

                    bindBody(viewBinding.notificationBody, item)
                }
                is NotificationGroupBinding -> {
                    val header = viewBinding.notificationHeader
                    val adapter = NotificationAdapter(context)

                    adapter.submitList(item.children)

                    viewBinding.recyclerView.apply {
                        this.adapter = adapter
                        layoutManager = LinearLayoutManagerWrapper(context)
                    }

                    if (item.expanded) {
                        adapter.nott()
                    } else {
                        adapter.showOnlyTitle()
                    }

                    bindHeader(header, item) {
                        item.expanded = item.expanded.not()
                        notifyItemChanged(adapterPosition)
                    }
                }
            }
        }

        private fun bindHeader(header: NotificationHeaderBinding, item: NotificationModel, onClick: () -> Unit) {
            val notification = item.notification
            val extras = notification.extras
            val label = item.label
            val postTime = item.postTime
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT) ?: BLANK
            var color = notification.color

            if (color == 0)
                color = colorDefault

            header.imageViewExpand.setColorFilter(color)
            header.textViewLabel.setTextColor(color)
            header.textViewLabel.text = label
            header.textViewPostTime.text = postTime.toDateString(simpleDateFormat)
            header.textViewSubText.text = subText

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                notification.smallIcon?.let {
                    header.imageViewSmallIcon.setImageIcon(it)
                    header.imageViewSmallIcon.setColorFilter(color)
                }
            }

            header.root.setOnClickListener {
                onClick.invoke()
            }

            // todo change.
            if (showOT)
                header.root.hide()
        }

        private fun bindBody(body: NotificationBodyBinding, item: NotificationModel) {
            val notification = item.notification
            val contentIntent = notification.contentIntent
            val extras = notification.extras
            val text = extras.getCharSequence(Notification.EXTRA_TEXT) ?: BLANK
            val title = extras.getCharSequence(Notification.EXTRA_TITLE) ?: BLANK

            body.root.setOnClickListener {
                try {
                    contentIntent.send()
                } catch (e: PendingIntent.CanceledException) {
                    Timber.e(e)
                }
            }

            body.textViewText.text = text
            body.textViewTitle.text = title

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                notification.getLargeIcon()?.let {
                    body.imageViewLargeIcon.setImageIcon(it)
                } ?: run {
                    body.imageViewLargeIcon.setImageIcon(null)
                }
            }

            // todo change.
            if (showOT) {
                body.textViewText.hide()
                body.imageViewLargeIcon.hide()
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
        val itemAnimator = recyclerView.itemAnimator

        if (itemAnimator is SimpleItemAnimator)
            itemAnimator.supportsChangeAnimations = false
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = when(viewType) {
            ViewType.BigPicture.value -> NotificationBigPictureBinding.inflate(layoutInflater, parent, false)
            ViewType.BigText.value -> NotificationBigTextBinding.inflate(layoutInflater, parent, false)
            ViewType.Inbox.value -> NotificationInboxBinding.inflate(layoutInflater, parent, false)
            ViewType.Media.value -> NotificationMediaBinding.inflate(layoutInflater, parent, false)
            ViewType.Messaging.value -> NotificationMessagingBinding.inflate(layoutInflater, parent, false)
            ViewType.Group.value -> NotificationGroupBinding.inflate(layoutInflater, parent, false)
            else -> NotificationBaseBinding.inflate(layoutInflater, parent, false)
        }

        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when(item.template) {
            NotificationStyle.BigPicture.value -> ViewType.BigPicture.value
            NotificationStyle.BigText.value -> ViewType.BigText.value
            NotificationStyle.Inbox.value -> ViewType.Inbox.value
            NotificationStyle.Media.value -> ViewType.Media.value
            NotificationStyle.Messaging.value -> ViewType.Messaging.value
            else -> {
                if (item.children.isNotEmpty())
                    ViewType.Group.value
                else
                    ViewType.Base.value
            }
        }
    }

    @Suppress("unused")
    private fun lastVisibleViewHolder(): ViewHolder? {
        var viewHolder: RecyclerView.ViewHolder? = null

        recyclerView?.let { recyclerView ->
            val layoutManager = recyclerView.layoutManager

            if (layoutManager is LinearLayoutManager) {
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                recyclerView.findViewHolderForAdapterPosition(lastVisibleItemPosition)?.let {
                    viewHolder = it
                }
            }
        }

        return viewHolder as? ViewHolder
    }

    // test code
    private var showOT = false
    private fun showOnlyTitle() {
        showOT = true

        val transitionSet = TransitionSet()

        transitionSet.addTransition(ChangeBounds())

        recyclerView?.let { TransitionManager.beginDelayedTransition(it, transitionSet) }
        notifyItemRangeChanged(0, itemCount)
    }
    private fun nott() {
        showOT = false

        val transitionSet = TransitionSet()

        transitionSet.addTransition(ChangeBounds())

        recyclerView?.let { TransitionManager.beginDelayedTransition(it, transitionSet) }
        notifyItemRangeChanged(0, itemCount)
    }
}

class DiffCallback: DiffUtil.ItemCallback<NotificationModel>() {
    override fun areItemsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
        return true
    }
}