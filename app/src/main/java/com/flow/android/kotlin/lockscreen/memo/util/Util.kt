package com.flow.android.kotlin.lockscreen.memo.util

import android.content.Context
import android.content.Intent
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.util.NEWLINE
import com.flow.android.kotlin.lockscreen.util.toDateString
import java.text.SimpleDateFormat
import java.util.*

fun share(context: Context, memo: Memo) {
    val intent = Intent(Intent.ACTION_SEND)
    val simpleDateFormat = SimpleDateFormat(context.getString(R.string.format_date_001), Locale.getDefault())
    val stringBuilder = StringBuilder()

    stringBuilder.append("${memo.modifiedTime.toDateString(simpleDateFormat)}$NEWLINE")
    stringBuilder.append("${memo.content}\n")
    stringBuilder.append("${memo.checkListToString()}$NEWLINE")

    intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
    intent.type = "text/plain"

    val chooser = Intent.createChooser(intent, context.getString(R.string.util_000))
    context.startActivity(chooser)
}