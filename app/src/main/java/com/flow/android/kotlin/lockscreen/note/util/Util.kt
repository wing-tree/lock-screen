package com.flow.android.kotlin.lockscreen.note.util

import android.content.Context
import android.content.Intent
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import com.flow.android.kotlin.lockscreen.util.NEWLINE

fun share(context: Context, note: Note) {
    val intent = Intent(Intent.ACTION_SEND)
    val stringBuilder = StringBuilder()

    stringBuilder.append(note.content)

    if (note.checklist.isNotEmpty())
        stringBuilder.append("$NEWLINE$NEWLINE")

    stringBuilder.append(note.checkListToString())

    intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
    intent.type = "text/plain"

    val chooser = Intent.createChooser(intent, context.getString(R.string.util_000))
    context.startActivity(chooser)
}