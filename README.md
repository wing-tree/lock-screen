# LockScreen

# Reference
## [A simple use of joinToString() Kotlin function to get comma separated strings for SQLite](https://medium.com/@SindkarP/a-simple-use-of-jointostring-kotlin-function-to-get-comma-separated-strings-for-sqlite-cbece2bcb499)
```
val string = calendarDisplays.map { it.id }.joinToString(separator = ", ") { "\"$it\"" }
```
## [Android Calendar Intent](https://itnext.io/android-calendar-intent-8536232ecb38)
## [Avoiding memory leaks when using Data Binding and View Binding](https://proandroiddev.com/avoiding-memory-leaks-when-using-data-binding-and-view-binding-3b91d571c150)
## [CalendarContract.Events](https://developer.android.com/reference/android/provider/CalendarContract.Events)
## [CalendarContract.EventsColumns](https://developer.android.com/reference/android/provider/CalendarContract.EventsColumns#ACCESS_CONFIDENTIAL)
## [How to decide font color in white or black depending on background color?](https://stackoverflow.com/questions/3942878/how-to-decide-font-color-in-white-or-black-depending-on-background-color)
```
@ColorInt
private fun getDateTimeTextColor(@ColorInt colorInt: Int): Int {
    var red = Color.red(colorInt) / 255.0
    var green = Color.green(colorInt) / 255.0
    var blue = Color.blue(colorInt) / 255.0

    if (red <= 0.03928)
        red /= 12.92
    else
        red = ((red + 0.055) / 1.055).pow(2.4)

    if (green <= 0.03928)
        green /= 12.92
    else
        green = ((green + 0.055) / 1.055).pow(2.4)

    if (blue <= 0.03928)
        blue /= 12.92
    else
        blue = ((red + 0.055) / 1.055).pow(2.4)

    val y = 0.2126 * red + 0.7152 * green + 0.0722 * blue

    return if (y > 0.179)
        Color.BLACK
    else
        Color.WHITE
}
```
## [How to get android lock screen wallpaper?](https://stackoverflow.com/questions/53881697/how-to-get-android-lock-screen-wallpaper)
```
private fun wallpaper(): Bitmap? {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        val wallpaperManager = WallpaperManager.getInstance(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val wallpaperFile = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK) ?: wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)

            wallpaperFile?.let {
                val bitmap = BitmapFactory.decodeFileDescriptor(wallpaperFile.fileDescriptor)

                try {
                    wallpaperFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                return bitmap
            }
        }

        return wallpaperManager.drawable.toBitmap()
    }

    return null
}
```
## [Palette](https://developer.android.com/reference/android/support/v7/graphics/Palette.html)
```
implementation 'androidx.palette:palette-ktx:1.0.0'
```
## [Selecting Colors with the Palette API](https://developer.android.com/training/material/palette-colors)
