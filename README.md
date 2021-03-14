# lock-screen

# References
## [Avoiding memory leaks when using Data Binding and View Binding](https://proandroiddev.com/avoiding-memory-leaks-when-using-data-binding-and-view-binding-3b91d571c150)
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
