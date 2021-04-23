package com.flow.android.kotlin.lockscreen.favoriteapp.entity

import android.graphics.drawable.Drawable

data class App(
    val icon: Drawable,
    val label: String,
    val packageName: String
)