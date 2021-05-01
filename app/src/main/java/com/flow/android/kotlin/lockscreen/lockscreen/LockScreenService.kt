package com.flow.android.kotlin.lockscreen.lockscreen

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.HomeBinding
import com.flow.android.kotlin.lockscreen.main.view.MainActivity
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.util.toPx
import timber.log.Timber
import java.util.concurrent.locks.Lock
import kotlin.Exception

class LockScreenService : Service() {
    object Action {
        const val HomeKeyPressed = "com.flow.android.kotlin.lockscreen.lockscreen" +
                ".LockScreenService.Action.HomePressed"
        const val RecentAppsPressed = "com.flow.android.kotlin.lockscreen.lockscreen" +
                ".LockScreenService.Action.RecentAppsPressed"
        const val StopSelf = "com.flow.android.kotlin.lockscreen.lockscreen" +
                ".LockScreenService.Action.StopSelf"
    }

    private val binding: HomeBinding by lazy {
        HomeBinding.inflate(LayoutInflater.from(this))
    }

    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    private val windowManager: WindowManager by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }

    private var isHomeVisible = false

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val showOnLockScreen = ConfigurationPreferences.getShowOnLockScreen(context)
            val displayAfterUnlocking = ConfigurationPreferences.getDisplayAfterUnlocking(context)

            when (intent.action) {
                Action.StopSelf -> {
                    stopSelf()
                    notificationManager.cancelAll()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    if (showOnLockScreen.not() || displayAfterUnlocking)
                        return

                    Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(this)
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    if (showOnLockScreen.not() || displayAfterUnlocking.not())
                        return

                    Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(this)
                    }
                }
                else -> {

                }
            }
        }
    }

    private val homeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                Action.HomeKeyPressed, Action.RecentAppsPressed -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(context))
                            showHome()
                    } else {
                        showHome()
                    }
                }
                else -> {

                }
            }
        }
    }

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        localBroadcastManager.registerReceiver(homeReceiver, IntentFilter().apply {
            addAction(Action.HomeKeyPressed)
            addAction(Action.RecentAppsPressed)
        })

        registerReceiver(
                broadcastReceiver,
                IntentFilter().apply {
                    addAction(Action.StopSelf)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_USER_PRESENT)
                }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_MIN
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)

            notificationChannel.description = CHANNEL_DESCRIPTION
            notificationChannel.setShowBadge(false)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_round_lock_open_24)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content_text))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MIN)

        val notification: Notification = builder.build()

        startForeground(1, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        try {
            localBroadcastManager.unregisterReceiver(homeReceiver)
            unregisterReceiver(broadcastReceiver)

            if (isHomeVisible)
                windowManager.removeViewImmediate(binding.root)
        } catch (e: Exception) {
            Timber.e(e)
            //stopSelf()
        }

        isHomeVisible = false

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showHome() {
        if (isHomeVisible)
            return

        binding.textView.setOnClickListener {
            windowManager.removeViewImmediate(binding.root)
            isHomeVisible = false
        }

        val type =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE

        val layoutParams = WindowManager.LayoutParams(
                type,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )

        layoutParams.height = windowHeight()
        layoutParams.windowAnimations = R.style.WindowAnimation

        windowManager.addView(binding.root, layoutParams)
        isHomeVisible = true
    }

    private fun windowHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() + insets.bottom + insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            val navigationBarHeight = navigationBarHeight()
            val statusBarHeight = statusBarHeight()

            displayMetrics.heightPixels + navigationBarHeight.times(2) + statusBarHeight.times(2)
        }
    }

    private fun navigationBarHeight(): Int {
        val identifier = resources.getIdentifier("navigation_bar_height", "dimen", "android")

        if (identifier > 0)
            return resources.getDimensionPixelSize(identifier)

        return 48.toPx
    }

    private fun statusBarHeight(): Int {
        val identifier = resources.getIdentifier("status_bar_height", "dimen", "android")

        if (identifier > 0)
            return resources.getDimensionPixelSize(identifier)

        return 25.toPx
    }

    @Suppress("SpellCheckingInspection")
    companion object {
        private const val CHANNEL_NAME = "com.flow.android.kotlin.lockscreen.lock_screen.channel_name"
        private const val CHANNEL_DESCRIPTION = "com.flow.android.kotlin.lockscreen.lock_screen.channel_description" // todo change real des.
        private const val CHANNEL_ID = "com.flow.android.kotlin.lockscreen.lock_screen.channel_id"
    }
}