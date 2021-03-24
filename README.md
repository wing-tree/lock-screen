# LockScreen

# Reference
## [[Android] AAC ViewModel 을 생성하는 6가지 방법 - ViewModelProvider](https://readystory.tistory.com/176)
## [[Android] Annotation Processor 만들기](https://www.charlezz.com/?p=1167)
### Annotation이란 ?
애노테이션은 자바 소스 코드에 추가 할 수있는 메타 데이터의 한 형태입니다.

### Annotation Processor란?
애노테이션 프로세서는 java 컴파일러의 플러그인의 일종입니다.

### Why Annotation?
#### 첫번째 이유, 빠릅니다.
Annotation Processor는 실제로 javac 컴파일러의 일부이므로 모든 처리가 런타임보다는 컴파일시간에 발생합니다. Annotation Processor가 정말 빠른 이유입니다. // 마무리할것.

## [android.view.View.systemUiVisibility deprecated. What is the replacement?](https://stackoverflow.com/questions/62577645/android-view-view-systemuivisibility-deprecated-what-is-the-replacement)
```
private fun hideSystemUi() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.setDecorFitsSystemWindows(false)
        window.insetsController?.let {
            it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }
}
```

## [A simple use of joinToString() Kotlin function to get comma separated strings for SQLite](https://medium.com/@SindkarP/a-simple-use-of-jointostring-kotlin-function-to-get-comma-separated-strings-for-sqlite-cbece2bcb499)
```
val string = calendarDisplays.map { it.id }.joinToString(separator = ", ") { "\"$it\"" }
```
## [Android Calendar Intent](https://itnext.io/android-calendar-intent-8536232ecb38)
## [Android - Making activity full screen with status bar on top of it](https://stackoverflow.com/questions/43511326/android-making-activity-full-screen-with-status-bar-on-top-of-it)
```
window?.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
)
```

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
private fun wallpaper(): Bitmap {
    val wallpaperManager = WallpaperManager.getInstance(this)
   
    return wallpaperManager.drawable.toBitmap()
}
```
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
## [ViewBinding - how to get binding for included layouts?](https://stackoverflow.com/questions/58730127/viewbinding-how-to-get-binding-for-included-layouts)
## [What is the difference between a View and widget in Android?](https://stackoverflow.com/questions/5168549/what-is-the-difference-between-a-view-and-widget-in-android/21541275)
As is stated in the `View` class docs:
> This class represents the basic building block for user interface components. A `View` occupies a rectangular area on the screen and is responsible for drawing and event handling. `View` is the base class for widgets, which are used to create interactive UI components (buttons, text fields, etc.).  

> The `ViewGroup` subclass is the base class for layouts, which are invisible containers that hold other `View`s (or other `ViewGroup`s) and define their layout properties.

Therefore a `View` is a base class for UI elements and a `Widget` is *loosely defined as* **any ready to use** `View`.

### View
A `View` is a base class for all UI elements. It, therefore, covers many different classes and concepts, including widgets, `ViewGroup`s and layouts. There is a root `View` attached to a Window instance which forms the basis of the `View` hierarchy. In general, the word `View` is usually used to describe UI elements in general, or to refer to abstract or base UI classes such as `ViewGroup`s.

### Widget
There are various definitions for this term, but most refer to a "ready to use" UI element, be it a `Button`, `ImageView`, `EditText`, etc. Note that some people consider widgets to be UI elements that are complete (not abstract) and are not containers (such as `ViewGroup`s (layouts/`ListView`s)). It's also worth noting that "widget" is a package name (`android.widget`) where the docs mention the following:

> The widget package contains (mostly visual) UI elements to use on your Application screen.

Therefore, it is reasonable to consider non-visual UI elements to also be widgets, as well as any class defined under the widget package. See here for a full list of classes in the widget package: http://developer.android.com/reference/android/widget/package-summary.html
