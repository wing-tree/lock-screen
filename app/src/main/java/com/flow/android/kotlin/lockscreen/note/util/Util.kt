package com.flow.android.kotlin.lockscreen.note.util

import android.content.Context
import android.content.Intent
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import com.flow.android.kotlin.lockscreen.util.NEWLINE
import com.flow.android.kotlin.lockscreen.util.toDateString
import java.text.SimpleDateFormat
import java.util.*

fun share(context: Context, note: Note) {
    val intent = Intent(Intent.ACTION_SEND)
    val simpleDateFormat = SimpleDateFormat(context.getString(R.string.format_date_001), Locale.getDefault())
    val stringBuilder = StringBuilder()

    stringBuilder.append("${note.modifiedTime.toDateString(simpleDateFormat)}$NEWLINE")
    stringBuilder.append("${note.content}\n")
    stringBuilder.append("${note.checkListToString()}$NEWLINE")

    intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
    intent.type = "text/plain"

    val chooser = Intent.createChooser(intent, context.getString(R.string.util_000))
    context.startActivity(chooser)
}