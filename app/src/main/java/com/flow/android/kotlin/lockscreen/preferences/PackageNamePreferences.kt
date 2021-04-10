package com.flow.android.kotlin.lockscreen.preferences

import android.content.Context

object PackageNamePreferences {
    @Suppress("SpellCheckingInspection")
    private object Name {
        const val PackageName = "com.flow.android.kotlin.lockscreen.preferences.name.package_name"
    }

    @Suppress("SpellCheckingInspection")
    private object Key {
        const val PackageName = "com.flow.android.kotlin.lockscreen.preferences.key.package_name"
    }

    fun getPackageNames(context: Context): Set<String> {
        val sharedPreferences = context.getSharedPreferences(Name.PackageName, Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(Key.PackageName, setOf()) ?: setOf()
    }

    fun addPackageName(context: Context, packageName: String) {
        val sharedPreferences = context.getSharedPreferences(Name.PackageName, Context.MODE_PRIVATE)
        val packageNames = getPackageNames(context).toMutableSet()

        packageNames.also {
            it.add(packageName)
            sharedPreferences.edit().putStringSet(Key.PackageName, it).apply()
        }
    }
}