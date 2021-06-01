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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
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

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var disposable: Disposable? = null

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

    override fun onCreate() {
        super.onCreate()

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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        disposable = NotificationBuilder.single(this, notificationManager)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->
                    startForeground(1, it.build())
                }

        return START_STICKY
    }

    override fun onDestroy() {
        try {
            localBroadcastManager.unregisterReceiver(homeReceiver)
            //IllegalArgumentException: Receiver not registered: todo
            unregisterReceiver(broadcastReceiver)

            if (isHomeVisible)
                windowManager.removeViewImmediate(binding.root)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            //stopSelf()
        } finally {
            disposable?.dispose()
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
}