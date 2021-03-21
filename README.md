# LockScreen

# Reference
## [[Android] AAC ViewModel 을 생성하는 6가지 방법 - ViewModelProvider](https://readystory.tistory.com/176)
## [A simple use of joinToString() Kotlin function to get comma separated strings for SQLite](https://medium.com/@SindkarP/a-simple-use-of-jointostring-kotlin-function-to-get-comma-separated-strings-for-sqlite-cbece2bcb499)
```
val string = calendarDisplays.map { it.id }.joinToString(separator = ", ") { "\"$it\"" }
```
## [Android Calendar Intent](https://itnext.io/android-calendar-intent-8536232ecb38)
## [Avoiding memory leaks when using Data Binding and View Binding](https://proandroiddev.com/avoiding-memory-leaks-when-using-data-binding-and-view-binding-3b91d571c150)
## [CalendarContract.Events](https://developer.android.com/reference/android/provider/CalendarContract.Events)
## [CalendarContract.EventsColumns](https://developer.android.com/reference/android/provider/CalendarContract.EventsColumns#ACCESS_CONFIDENTIAL)
## [Cannot read recurring events from android calendar programmatically](https://stackoverflow.com/questions/7130025/cannot-read-recurring-events-from-android-calendar-programmatically)
### [Instances table](https://developer.android.com/guide/topics/providers/calendar-provider.html#instances)
```
@Suppress("SpellCheckingInspection")
@SuppressLint("Recycle")
fun instances(contentResolver: ContentResolver, eventId: String, DTSTART: Calendar, DTEND: Calendar): ArrayList<Event>? {

    val events = arrayListOf<Event>()

    val selection = "${CalendarContract.Instances.EVENT_ID} = ?"
    val selectionArgs: Array<String> = arrayOf(eventId)

    val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
    ContentUris.appendId(builder, DTSTART.timeInMillis)
    ContentUris.appendId(builder, DTEND.timeInMillis)

    val cursor = contentResolver.query(
            builder.build(),
            Instances.projection,
            selection,
            selectionArgs,
            null
    ) ?: return null

    while (cursor.moveToNext()) {
        val begin = cursor.getLongOrNull(Instances.Index.BEGIN) ?: 0L
        val calendarDisplayName = cursor.getStringOrNull(Instances.Index.CALENDAR_DISPLAY_NAME) ?: BLANK
        val calendarId = cursor.getLongOrNull(Instances.Index.CALENDAR_ID) ?: continue
        val end = cursor.getLongOrNull(Instances.Index.END) ?: 0L
        val id = cursor.getLongOrNull(Instances.Index.EVENT_ID) ?: continue
        val rrule = cursor.getStringOrNull(Instances.Index.RRULE) ?: BLANK
        val title = cursor.getStringOrNull(Instances.Index.TITLE) ?: BLANK

        events.add(Event(
                begin = begin,
                calendarDisplayName = calendarDisplayName,
                calendarId = calendarId,
                end = end,
                id = id,
                rrule = rrule,
                title = title
        ))
    }

    return events
}

@SuppressLint("Recycle")
fun events(contentResolver: ContentResolver, calendarDisplays: List<CalendarDisplay>): ArrayList<Event>? {

    val events = arrayListOf<Event>()

    @Suppress("LocalVariableName", "SpellCheckingInspection")
    val DTSTART = Calendar.getInstance()

    @Suppress("LocalVariableName", "SpellCheckingInspection")
    val DTEND = Calendar.getInstance()

    DTSTART.set(Calendar.HOUR_OF_DAY, 0)
    DTSTART.set(Calendar.MINUTE, 0)
    DTSTART.set(Calendar.SECOND, 0)

    DTEND.set(Calendar.HOUR_OF_DAY, 0)
    DTEND.set(Calendar.MINUTE, 0)
    DTEND.set(Calendar.SECOND, 0)
    DTEND.add(Calendar.DATE, 1)

    val string = calendarDisplays.map { it.id }.joinToString(separator = ", ") { "\"$it\"" }
    val selection = "(${CalendarContract.Events.CALENDAR_ID} IN ($string)) AND " +
            "(${CalendarContract.Events.DELETED} = 0)"

    val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            Events.projection,
            selection,
            null,
            null
    ) ?: return null

    cursor.moveToFirst()

    @Suppress("SpellCheckingInspection")
    while (cursor.moveToNext()) {
        @Suppress("LocalVariableName")
        val _id = cursor.getLongOrNull(Events.Index._ID) ?: continue
        val title = cursor.getStringOrNull(Events.Index.TITLE) ?: BLANK

        Timber.d("events")
        Timber.d("_id: $_id")
        Timber.d("title: $title")

        instances(contentResolver, _id.toString(), DTSTART, DTEND)?.let { instances ->
            events.addAll(instances)
        }
    }

    return events
}
```
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
## [Using Sync Adapter to transfer data in android](https://nyamebismark12-nb.medium.com/using-sync-adapter-to-transfer-data-ad1e6c3f2d64)
