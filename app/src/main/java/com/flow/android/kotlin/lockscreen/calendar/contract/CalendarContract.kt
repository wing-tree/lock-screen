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
            Intent(Intent.ACTION_INSERT).setData(uri)
        } ?: run {
            val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)

            output = CalendarLoader.RequestCode.InsertEvent
            intent
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Int {
        return output
    }
}