package com.flow.android.kotlin.lockscreen.calendar.contract

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.activity.result.contract.ActivityResultContract
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.calendar.model.Model

class CalendarContract: ActivityResultContract<Model.Event?, Int>() {
    private var output = 0

    override fun createIntent(context: Context, input: Model.Event?): Intent {
        return input?.let {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, it.eventId)

            output = CalendarLoader.RequestCode.EditEvent

            Intent(Intent.ACTION_INSERT).apply {
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it.begin)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, it.end)
            }.setData(uri)
        } ?: run {
            output = CalendarLoader.RequestCode.InsertEvent

            Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Int {
        return output
    }
}