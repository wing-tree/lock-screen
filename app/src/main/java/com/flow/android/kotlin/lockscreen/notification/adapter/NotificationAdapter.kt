package com.flow.android.kotlin.lockscreen.notification.adapter

import android.app.Notification
import android.app.PendingIntent.CanceledException
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.NotificationBinding
import com.flow.android.kotlin.lockscreen.notification.model.NotificationModel
import com.flow.android.kotlin.lockscreen.util.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter : ListAdapter<NotificationModel, NotificationAdapter.ViewHolder>(
    DiffCallback()
) {
    private var layoutInflater: LayoutInflater? = null

    class ViewHolder private constructor(private val binding: NotificationBinding): RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(item: NotificationModel) {
            val context = binding.root.context

            val label = item.label
            val notification = item.notification
            val postTime = item.postTime

            println("noti group: " + notification.group)


            val extras: Bundle = notification.extras
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT) ?: BLANK
            val text = extras.getCharSequence(Notification.EXTRA_TEXT) ?: BLANK
            val title = extras.getCharSequence(Notification.EXTRA_TITLE) ?: BLANK

            // style 에 따라 처리 필요할 듯.
            val bigContentText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            val textLines = extras.getCharSequence(Notification.EXTRA_TEXT_LINES)

            Timber.d("$bigContentText,,, $textLines")
            val keysett = extras.keySet()

            val contentIntent = notification.contentIntent
            val color = notification.color

            Notification.EXTRA_SUMMARY_TEXT
            Notification.EXTRA_TEXT_LINES
            notification.extras

            //        // todo remove test..
//        val nm = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val ss = nm.activeNotifications.mapNotNull { it.notification }
//        if (ss.isNotEmpty()) {
//            val a = ss[0].extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
//            val b = ss[0].extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
//            val c = ss[0].extras.getCharSequence(Notification.EXTRA_TEMPLATE)
//            val cv = ss[0].extras.getCharSequence(Notification.EXTRA_PICTURE) //
//
//            println("TTTTTTT: ${a.toString()},, $b,, $c,, or ${a?.map { it }.toString()}")
//        }


            Notification.CATEGORY_SYSTEM // 상수로 되어 있다. 시스템 알림은 제ㅐ외하면 될 듯.

            binding.textViewLabel.text = label
            binding.textViewLabel.setTextColor(color)

            binding.textViewPostTime.text = postTime.toDateString(
                SimpleDateFormat(
                    context.getString(
                        R.string.format_time_000
                    ), Locale.getDefault()
                )
            )
            binding.textViewSubText.text = subText
            binding.textViewText.text = text
            binding.textViewTitle.text = title

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val smallIcon: Icon? = notification.smallIcon
                val largeIcon: Icon? = notification.getLargeIcon()

                binding.imageViewSmallIcon.setImageIcon(smallIcon)
                binding.imageViewLargeIcon.setImageIcon(largeIcon)
            }

            binding.linearLayoutAction.removeAllViews()

            if (notification.actions.isNullOrEmpty())
                binding.linearLayoutAction.hide()
            else {
                binding.linearLayoutAction.show()

                for (action in notification.actions) {
                    val textView = TextView(context).apply {
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

                    binding.linearLayoutAction.addView(textView)
                }
            }

            binding.root.setOnClickListener {
                try {
                    contentIntent.send()
                } catch (e: CanceledException) {
                    Timber.e(e)
                }
            }
        }

        companion object {
            fun from(binding: NotificationBinding): ViewHolder {
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = this.layoutInflater ?: LayoutInflater.from(parent.context)

        this.layoutInflater = layoutInflater

        return ViewHolder.from(NotificationBinding.inflate(layoutInflater, parent, false))
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