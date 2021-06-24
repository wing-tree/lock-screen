package com.flow.android.kotlin.lockscreen.notification.adapter

import android.app.Notification
import android.app.PendingIntent.CanceledException
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaSession2
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.*
import com.flow.android.kotlin.lockscreen.notification.model.NotificationModel
import com.flow.android.kotlin.lockscreen.util.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val applicationContext: Context) :
    ListAdapter<NotificationModel, NotificationAdapter.ViewHolder>(DiffCallback()) {
    private object NotificationStyle {
        const val BigPicture = "BigPictureStyle"
        const val BigText = "BigTextStyle"
        const val Inbox = "InboxStyle"
        const val Media = "MediaStyle"
        const val Messaging = "MessagingStyle"
    }

    private var layoutInflater: LayoutInflater? = null

    class ViewHolder private constructor(private val binding: NotificationBaseBinding): RecyclerView.ViewHolder(
        binding.root
    ) {
        private val simpleDateFormat by lazy {
            val context = binding.root.context

            SimpleDateFormat(context.getString(R.string.format_time_000), Locale.getDefault())
        }

        fun bind(item: NotificationModel) {

            val context = binding.root.context

            val label = item.label
            val notification = item.notification
            val postTime = item.postTime

            val extras: Bundle = notification.extras
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT) ?: BLANK
            val text = extras.getCharSequence(Notification.EXTRA_TEXT) ?: BLANK
            val title = extras.getCharSequence(Notification.EXTRA_TITLE) ?: BLANK

            bindBaseNotification(binding, item)

            ///////
            // style 에 따라 처리 필요할 듯.
            val template = extras.getCharSequence(Notification.EXTRA_TEMPLATE)
            Notification.EXTRA_PICTURE
            extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)

            println("TTTTTTTTEMEMEME: $template")
            println("TTTTTTTTEMEMEMESSS: ${template.toString()}")
            println("YYYYYYYYYYY: ${template?.javaClass?.simpleName}")
            println("WWWWWWW: :" + Notification.BigTextStyle::class.java.simpleName) // contain으로 . endwith로 검사하면될듯.
            println("WWWWWW2222W: :" + Notification.BigTextStyle::class.java.name) // 이걸로.

            Notification.EXTRA_MEDIA_SESSION

            val token = extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)



            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            val textLines = extras.getCharSequence(Notification.EXTRA_TEXT_LINES)

            val keysett = extras.keySet()




            Notification.CATEGORY_SYSTEM // 상수로 되어 있다. 시스템 알림은 제ㅐ외하면 될 듯.
            /////
        }

        private fun bindBaseNotification(binding: NotificationBaseBinding, item: NotificationModel) {
            val context = binding.root.context

            val label = item.label
            val notification = item.notification
            val postTime = item.postTime

            val contentIntent = notification.contentIntent
            val color = notification.color
            val extras: Bundle = notification.extras

            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT) ?: BLANK
            val template = extras.getCharSequence(Notification.EXTRA_TEMPLATE) ?: BLANK
            val text = extras.getCharSequence(Notification.EXTRA_TEXT) ?: BLANK
            val title = extras.getCharSequence(Notification.EXTRA_TITLE) ?: BLANK

            binding.root.setOnClickListener {
                try {
                    contentIntent.send()
                } catch (e: CanceledException) {
                    Timber.e(e)
                }
            }

            binding.imageViewLargeIcon.hide(true)
            binding.textViewText.text = text
            binding.textViewTitle.text = title

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                notification.getLargeIcon()?.let {
                    binding.imageViewLargeIcon.show()
                    binding.imageViewLargeIcon.setImageIcon(it)
                }

            bindHeader(binding.notificationHeader, item)
            addActions(binding.linearLayoutAction, notification.actions)
        }

        private fun bindHeader(header: NotificationHeaderBinding, item: NotificationModel) {
            val label = item.label
            val notification = item.notification
            val postTime = item.postTime

            val color = notification.color
            val extras: Bundle = notification.extras

            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT) ?: BLANK

            header.imageViewSmallIcon

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                notification.smallIcon?.let { header.imageViewSmallIcon.setImageIcon(it) }

            header.textViewLabel.setTextColor(color)
            header.textViewLabel.text = label
            header.textViewPostTime.text = postTime.toDateString(simpleDateFormat)
            header.textViewSubText.text = subText

            header.root.setOnClickListener {
                if (item.expanded) {
                    item.expanded = false
                    collapseViewStub(binding.frameLayout)
                } else {
                    item.expanded = true
                    expandViewStub(binding, notification)
                }
            }
        }

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

        private fun collapseViewStub(container: FrameLayout) {
            container.hide()
        }

        private fun expandViewStub(binding: NotificationBaseBinding, notification: Notification) {
            val extras = notification.extras
            val template = extras.getCharSequence(Notification.EXTRA_TEMPLATE) ?: BLANK

            when {
                template.endsWith(NotificationStyle.BigPicture) -> {
                    binding.notificationBigPicture.root.show()

                    extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)?.let {
                        binding.notificationBigPicture.imageViewBigPicture.setImageBitmap(it)
                    }
                }
                template.endsWith(NotificationStyle.BigText) -> {
                    val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                    val text = extras.getCharSequence(Notification.EXTRA_TEXT) ?: BLANK
                    binding.notificationBigText.root.show()
                    binding.notificationBigText.textViewBigText.text = bigText ?: text
                }
                template.endsWith(NotificationStyle.Inbox) -> {
                    val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                    val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) ?: emptyArray()
                    val title = extras.getCharSequence(Notification.EXTRA_TITLE) ?: BLANK

                    binding.notificationInbox.root.show()
                    with(binding.notificationInbox) {
                        textViewBigContentTitle.text = bigText ?: title

                        textViewLine0.hide()
                        textViewLine1.hide()
                        textViewLine2.hide()
                        textViewLine3.hide()
                        textViewLine4.hide()
                        textViewLine5.hide()

                        for ((index, textLine) in textLines.withIndex()) {
                            if (index > 5)
                                break

                            val textView = when(index) {
                                0 -> textViewLine0
                                1 -> textViewLine1
                                2 -> textViewLine2
                                3 -> textViewLine3
                                4 -> textViewLine4
                                5 -> textViewLine5
                                else -> throw IllegalStateException("Invalid index")
                            }

                            textView.show()
                            textView.text = textLine
                        }
                    }
                }
                template.endsWith(NotificationStyle.Media) -> {

                }
                template.endsWith(NotificationStyle.Messaging) -> {

                }
                else -> {

                }
            }
        }

        companion object {
            fun from(binding: NotificationBaseBinding): ViewHolder {
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = this.layoutInflater ?: LayoutInflater.from(parent.context)

        this.layoutInflater = layoutInflater

        return ViewHolder.from(NotificationBaseBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
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