package com.flow.android.kotlin.lockscreen.main.view

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ActivityOptions
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.view.MotionEvent
import android.view.View.GONE
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.application.ApplicationUtil
import com.flow.android.kotlin.lockscreen.base.BaseActivity
import com.flow.android.kotlin.lockscreen.calendar.viewmodel.CalendarViewModel
import com.flow.android.kotlin.lockscreen.preference.view.PreferenceActivity
import com.flow.android.kotlin.lockscreen.databinding.ActivityMainBinding
import com.flow.android.kotlin.lockscreen.devicecredential.DeviceCredential
import com.flow.android.kotlin.lockscreen.devicecredential.RequireDeviceCredential
import com.flow.android.kotlin.lockscreen.lockscreen.service.LockScreenService
import com.flow.android.kotlin.lockscreen.main.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.main.notification.ManageOverlayPermissionNotificationBuilder
import com.flow.android.kotlin.lockscreen.main.torch.Torch
import com.flow.android.kotlin.lockscreen.main.view.MainActivity.Unlock.endRange
import com.flow.android.kotlin.lockscreen.main.view.MainActivity.Unlock.outOfEndRange
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel
import com.flow.android.kotlin.lockscreen.main.viewmodel.Refresh
import com.flow.android.kotlin.lockscreen.memo.viewmodel.MemoViewModel
import com.flow.android.kotlin.lockscreen.permission.PermissionChecker
import com.flow.android.kotlin.lockscreen.permission._interface.OnPermissionAllowClickListener
import com.flow.android.kotlin.lockscreen.permission.view.PermissionRationaleDialogFragment
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.shortcut.viewmodel.ShortcutViewModel
import com.flow.android.kotlin.lockscreen.util.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import timber.log.Timber
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : BaseActivity(),
        OnPermissionAllowClickListener, RequireDeviceCredential<Unit> {
    private val duration = 300L

    private val viewBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()
    private val memoViewModel: MemoViewModel by viewModels()
    private val shortcutViewModel: ShortcutViewModel by viewModels()

    private val torch: Torch by lazy {
        Torch(viewBinding)
    }

    private val delayMillis = 800L
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private var handler: Handler? = null

    private object Unlock {
        const val endRange = 600F
        var x = 0F
        var y = 0F
        var outOfEndRange = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        if (Preference.LockScreen.getShowOnLockScreen(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)

                val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager

                keyguardManager.requestDismissKeyguard(this, null)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                )
            }
        }

        window?.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        notificationManager.cancel(ManageOverlayPermissionNotificationBuilder.ID)
        startService()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionRationaleDialogFragment.permissionsGranted(this).not()) {
                PermissionRationaleDialogFragment().also {
                    it.show(supportFragmentManager, it.tag)
                }
            } else
                calendarViewModel.postValue()
        } else
            calendarViewModel.postValue()

        memoViewModel
        shortcutViewModel

        initializeViews()
        registerLifecycleObservers()
        initializeActivityResultLaunchers()
        firstRun()
    }

    override fun onStart() {
        super.onStart()

        handler = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.hasManageOverlayPermission().not()) {
                PermissionRationaleDialogFragment().also {
                    it.show(supportFragmentManager, it.tag)
                }
            }
        }
    }

    override fun onPause() {
        with(viewBinding.centerAlignedTabLayout.selectedTabPosition) {
            Preference.putSelectedTabIndex(this@MainActivity, this)
        }

        super.onPause()
    }

    override fun onDestroy() {
        sendBroadcast(Intent(LockScreenService.Action.MainActivityDestroyed))
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (PermissionChecker.dismissSnackbar())
            return

        if (Preference.LockScreen.getUnlockWithBackKey(this))
            finish()
        else
            viewBinding.frameLayoutRipple.forceRippleAnimation()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun restoreVisibility() {
        viewBinding.frameLayoutRipple.hideRipple()
        viewBinding.constraintLayout.scale(1F, duration)
        viewBinding.imageViewUnlock.scale(1F, duration)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeViews() {
        viewBinding.linearLayoutUnlock.setOnTouchListener { _, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Unlock.x = event.x
                    Unlock.y = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    viewBinding.frameLayoutRipple.showRipple()

                    val distance = sqrt((Unlock.x - event.x).pow(2) + (Unlock.y - event.y).pow(2))
                    var scale = abs(endRange - distance * 0.45F) / endRange

                    when {
                        scale >= 1F -> scale = 1F
                        scale < 0.75F -> scale = 0.75F
                    }

                    val alpha: Float = (scale - 0.75F) * 4

                    viewBinding.constraintLayout.alpha = alpha
                    viewBinding.constraintLayout.scaleX = scale
                    viewBinding.constraintLayout.scaleY = scale
                    viewBinding.imageViewUnlock.alpha = alpha
                    viewBinding.imageViewUnlock.scaleX = scale
                    viewBinding.imageViewUnlock.scaleY = scale

                    outOfEndRange = distance * 1.25F > endRange * 0.75F
                }
                MotionEvent.ACTION_UP -> {
                    if (outOfEndRange) {
                        if (DeviceCredential.requireUnlock(this))
                            confirmDeviceCredential(Unit)
                        else
                            finish()
                    } else
                        restoreVisibility()
                }
            }

            true
        }

        viewBinding.imageViewCamera.setOnClickListener {
            Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                            ApplicationUtil.findCameraAppPackageName(this@MainActivity)?.let {
                                var intent: Intent? = null

                                try {
                                    intent = packageManager.getLaunchIntentForPackage(it)
                                } catch (ignored: Exception) {
                                    Timber.e(ignored)
                                    // 카메라 없으니 꺼지렴.
                                }

                                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                startActivity(intent)
                            } ?: run {
                                // 꺼지라고 하셈. 못찾앗으니 꺼지렴.
                            }
                        }
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {}

                    override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest?,
                            token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }

        viewBinding.imageViewSettings.setOnClickListener {
            Intent(this, PreferenceActivity::class.java).also {
                it.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)

                getActivityResultLauncher(PreferenceActivity.Name.PreferenceChanged)?.launch(it)
                overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewBinding.imageViewHighlight.setOnClickListener {
                torch.toggle()
            }
        } else
            viewBinding.imageViewHighlight.visibility = GONE

        TabLayoutInitializer.initialize(
                viewBinding.centerAlignedTabLayout,
                viewBinding.viewPager2,
                FragmentStateAdapter(this)
        )
    }

    private fun registerLifecycleObservers() {
        viewModel.refresh.observe(this, {
            it ?: return@observe

            when(it) {
                Refresh.Calendar -> calendarViewModel.callRefresh()
                Refresh.Memo -> memoViewModel.callRefresh()
                Refresh.Shortcut -> shortcutViewModel.callRefresh()
            }
        })

        viewModel.showRequestCalendarPermissionSnackbar.observe(this, {
            PermissionChecker.showRequestCalendarPermissionSnackbar(
                    viewBinding.coordinatorLayout,
                    getActivityResultLauncher(PermissionChecker.Calendar.KEY)
            )
        })
    }

    private fun initializeActivityResultLaunchers() {
        putActivityResultLauncher(
                DeviceCredential.Key.ConfirmDeviceCredential,
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                    finish()
                }
        )

        putActivityResultLauncher(
                PreferenceActivity.Name.PreferenceChanged,
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        val preferenceChanged = result.data?.getParcelableExtra<Preference.PreferenceChanged>(
                                PreferenceActivity.Name.PreferenceChanged
                        ) ?: return@registerForActivityResult

                        viewModel.refresh(preferenceChanged)
                    }
                }
        )

        putActivityResultLauncher(
                PermissionChecker.Calendar.KEY,
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    calendarViewModel.postValue()
                }
        )
    }

    private fun checkManageOverlayPermission() {
        if (PermissionChecker.hasManageOverlayPermission())
            startService()
        else {
            val uri = Uri.fromParts("package", packageName, null)
            @SuppressLint("InlinedApi")
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            }

            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

            handler = Handler(mainLooper)

            handler?.postDelayed(object : Runnable {
                @TargetApi(23)
                override fun run() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                        return

                    if (Settings.canDrawOverlays(this@MainActivity)) {
                        Intent(this@MainActivity, MainActivity::class.java).run {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            val customAnimation = ActivityOptions.makeCustomAnimation(
                                    this@MainActivity,
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                            )

                            startActivity(this, customAnimation.toBundle())
                        }

                        handler = null
                        return
                    }

                    handler?.postDelayed(this, delayMillis)
                }
            }, delayMillis)
        }
    }

    private fun startService() {
        val intent = Intent(applicationContext, LockScreenService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent)
        else
            startService(intent)
    }

    private fun firstRun() {
        if (Preference.getFirstRun(this)) {
            memoViewModel.insert(
                    Memo(
                            content = getString(R.string.memo_fragment_000),
                            color = ContextCompat.getColor(this, R.color.white),
                            id = -20210513L,
                            modifiedTime = System.currentTimeMillis(),
                            priority = System.currentTimeMillis()
                    )
            )

            Preference.putFirstRun(this, false)
        }
    }

    override fun onPermissionAllowClick() {
        PermissionChecker.checkPermissions(this, listOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
        ), {
            calendarViewModel.postValue()
        }, {
            calendarViewModel.callDisableCalendarControlViews()
            viewModel.callShowRequestCalendarPermissionSnackbar()
        }) {
            checkManageOverlayPermission()
        }
    }

    override fun onPermissionDenyClick() {
        if (PermissionChecker.hasManageOverlayPermission())
            startService()
        else
            finish()

        if (PermissionChecker.hasCalendarPermission().not()) {
            if (isFinishing.not()) {
                calendarViewModel.callDisableCalendarControlViews()
                viewModel.callShowRequestCalendarPermissionSnackbar()
            }
        }
    }

    override fun confirmDeviceCredential(value: Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DeviceCredential.confirmDeviceCredential(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissCancelled() {
                    super.onDismissCancelled()
                    restoreVisibility()
                }

                override fun onDismissError() {
                    super.onDismissError()
                    restoreVisibility()
                }

                override fun onDismissSucceeded() {
                    super.onDismissSucceeded()
                    finish()
                }
            })
        } else {
            DeviceCredential.confirmDeviceCredential(
                    this,
                    getActivityResultLauncher(DeviceCredential.Key.ConfirmDeviceCredential)
            )

            restoreVisibility()
        }
    }
}