package com.flow.android.kotlin.lockscreen.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import androidx.annotation.ColorInt
import com.flow.android.kotlin.lockscreen.R
import java.text.SimpleDateFormat
import java.util.*


fun Long.toDateString(simpleDateFormat: SimpleDateFormat): String {
    return simpleDateFormat.format(Date(this))
}

fun <T> List<T>.diff(list: List<T>) = Diff(
        first = list.filterNot { this.contains(it) },
        second = this.filterNot { list.contains(it) }
)

data class Diff<T>(
        val first: List<T>,
        val second: List<T>
)

val Float.toDp get() = this / Resources.getSystem().displayMetrics.density
val Float.toPx get() = this * Resources.getSystem().displayMetrics.density

val Int.toDp get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toPx get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun rippleDrawable(@ColorInt colorPressed: Int, drawable: Drawable): RippleDrawable {
    val colorStateList = ColorStateList(arrayOf(intArrayOf()), intArrayOf(colorPressed))

    return RippleDrawable(colorStateList, drawable, null)
}

fun shareApplication(context: Context) {
    val intent = Intent(Intent.ACTION_SEND)
    val text = "https://play.google.com/store/apps/details?id=${context.packageName}"

    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)

    Intent.createChooser(intent, context.getString(R.string.util_001)).run {
        context.startActivity(this)
    }
}

fun versionName(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return BLANK
    }
}