package com.flow.android.kotlin.lockscreen.notification.adapter

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import androidx.transition.TransitionSet.ORDERING_SEQUENTIAL
import androidx.transition.TransitionSet.ORDERING_TOGETHER
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.*
import com.flow.android.kotlin.lockscreen.notification.model.NotificationModel
import com.flow.android.kotlin.lockscreen.util.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class NotificationAdapter(private val context: Context) :
    ListAdapter<NotificationModel, NotificationAdapter.ViewHolder>(DiffCallback()) {

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
        Base(5)
    }

    private val layoutInflater by lazy { LayoutInflater.from(context) }
    private val simpleDateFormat by lazy {
        SimpleDateFormat(context.getString(R.string.format_time_000), Locale.getDefault())
    }

    inner class ViewHolder(private val viewBinding: ViewBinding): RecyclerView.ViewHolder(
            viewBinding.root
    ) {
        fun root() = viewBinding.root

        private fun addActions(container: LinearLayout, actions: Array<Notification.Action>?) {
            container.removeAllViews()

            if (actions.isNullOrEmpty())
                container.hide()
            else {
                container.show()

                for (action in actions) {
                    val textView = TextView(container.context).apply {
                        gravity = Gravity.CENTER
                        this.text = action.title
                        setOnClickListener {
                            action.actionIntent.send()
                        }
                    }

                    textView.layoutParams = TableLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1F
                    )

                    container.addView(textView)
                }
            }
        }

        fun bind(item: NotificationModel) {
            val notification = item.notification
            val extras = notification.extras
            val text = extras.getCharSequence(Notification.EXTRA_TEXT) ?: BLANK

            when(viewBinding) {
                is NotificationBigPictureBinding -> {
                    bindHeader(viewBinding.notificationHeader, item) {

                    }

                    bindBody(viewBinding.notificationBody, item)
                }
                is NotificationBigTextBinding -> {
                    val header = viewBinding.notificationHeader
                    val body = viewBinding.notificationBody
                    val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: text

                    if (item.expanded) {
                        body.textViewText.post {
                            val ellipsisStart = body.textViewText.layout.getEllipsisStart(0)

                            // todo ;;;

                            body.textViewText.text = bigText.subSequence(0, ellipsisStart)
                            viewBinding.textViewBigText.text = bigText.subSequence(ellipsisStart, bigText.length)
                        }

                        viewBinding.linearLayoutFooter.show()
                    } else {
                        viewBinding.linearLayoutFooter.hide()
                    }

                    bindHeader(header, item) {
                        item.expanded = item.expanded.not()

                        notifyItemChanged(adapterPosition)
                    }

                    bindBody(body, item)
                    addActions(viewBinding.linearLayoutAction, notification.actions)
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
                    bindHeader(viewBinding.notificationHeader, item) {

                    }
                    bindBody(viewBinding.notificationBody, item)
                }
            }
        }

        private fun bindHeader(header: NotificationHeaderBinding, item: NotificationModel, onClick: () -> Unit) {
            val notification = item.notification
            val color = notification.color
            val extras = notification.extras
            val label = item.label
            val postTime = item.postTime
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT) ?: BLANK

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
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = when(viewType) {
            ViewType.BigPicture.value -> NotificationBigPictureBinding.inflate(layoutInflater, parent, false)
            ViewType.BigText.value -> NotificationBigTextBinding.inflate(layoutInflater, parent, false)
            ViewType.Inbox.value -> NotificationInboxBinding.inflate(layoutInflater, parent, false)
            ViewType.Media.value -> NotificationMediaBinding.inflate(layoutInflater, parent, false)
            ViewType.Messaging.value -> NotificationMessagingBinding.inflate(layoutInflater, parent, false)
            else -> NotificationBaseBinding.inflate(layoutInflater, parent, false)
        }

        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position).template) {
            NotificationStyle.BigPicture.value -> ViewType.BigPicture.value
            NotificationStyle.BigText.value -> ViewType.BigText.value
            NotificationStyle.Inbox.value -> ViewType.Inbox.value
            NotificationStyle.Media.value -> ViewType.Media.value
            NotificationStyle.Messaging.value -> ViewType.Messaging.value
            else -> ViewType.Base.value
        }
    }

    @Suppress("unused")
    private fun lastVisibleViewHolder(): RecyclerView.ViewHolder? {
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

        return viewHolder
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