package com.flow.android.kotlin.lockscreen.preference.view

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseActivity
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.databinding.ActivityPreferenceBinding
import com.flow.android.kotlin.lockscreen.permission.PermissionChecker
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.preference.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.preference.calendar.view.CalendarPreferenceFragment
import com.flow.android.kotlin.lockscreen.preference.display.view.DisplayPreferenceFragment
import com.flow.android.kotlin.lockscreen.preference.lockscreen.view.LockScreenPreferenceFragment
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.review.Review
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper
import com.flow.android.kotlin.lockscreen.util.shareApplication
import com.flow.android.kotlin.lockscreen.util.versionName

class PreferenceActivity: BaseActivity() {
    private val viewBinding by lazy { ActivityPreferenceBinding.inflate(layoutInflater) }

    private val activity = this
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())

    private val adapter: PreferenceAdapter by lazy {
        PreferenceAdapter(arrayListOf(
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_round_calendar_today_24),
                        summary = getString(R.string.configuration_activity_006),
                        onClick = { _, _ ->
                            PermissionChecker.checkPermissions(this, listOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR
                            ), {
                                addFragment(CalendarPreferenceFragment())
                            }, {
                                PermissionChecker.showRequestCalendarPermissionSnackbar(
                                    viewBinding.root,
                                    getActivityResultLauncher(PermissionChecker.Calendar.KEY)
                                )
                            })
                        },
                        title = getString(R.string.calendar)
                ),
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_mobile_48px),
                        summary = getString(R.string.configuration_activity_002),
                        onClick = { _, _ ->
                            addFragment(DisplayPreferenceFragment())
                        },
                        title = getString(R.string.configuration_activity_001)
                ),
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_round_screen_lock_portrait_24),
                        summary = getString(R.string.configuration_activity_000),
                        onClick = { _, _ ->
                            addFragment(LockScreenPreferenceFragment())
                        },
                        title = getString(R.string.configuration_activity_003)
                ),
                AdapterItem.Space(),
                AdapterItem.Content(
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_round_share_24),
                        onClick = { _, _ ->
                            shareApplication(this)
                        },
                        title = getString(R.string.configuration_activity_007)
                ),
                AdapterItem.Content(
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_round_rate_review_24),
                        onClick = { _, _ ->
                            Review.launchReviewFlow(this)
                        },
                        title = getString(R.string.configuration_activity_008)
                ),
                AdapterItem.Content(
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_round_info_24),
                        isClickable = false,
                        summary = versionName(this),
                        title = getString(R.string.configuration_activity_009)
                ),
        ))
    }

    object Name {
        private const val Prefix = "com.flow.android.kotlin.lockscreen.preference.view" +
                ".PreferenceActivity.Name"
        const val PreferenceChanged = "$Prefix.PreferenceChange"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        initializeToolbar(viewBinding.toolbar)
        Preference.initializeOldValues(this)

        viewBinding.recyclerView.apply {
            adapter = this@PreferenceActivity.adapter
            layoutManager = LinearLayoutManagerWrapper(activity)
        }

        val uncheckedCalendarIds = Preference.Calendar.getUncheckedCalendarIds(this)
        val checkBoxItems = CalendarLoader.calendars(contentResolver).map {
                CheckBoxItem(
                        isChecked = uncheckedCalendarIds.contains(it.id.toString()).not(),
                        text = it.name,
                        onCheckedChange = { isChecked ->
                            if (isChecked)
                                Preference.Calendar.removeUncheckedCalendarId(this, it.id.toString())
                            else
                                Preference.Calendar.addUncheckedCalendarId(this, it.id.toString())
                        }
                )
            }

        checkBoxAdapter.addAll(checkBoxItems)

        putActivityResultLauncher(
            PermissionChecker.Calendar.KEY,
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (PermissionChecker.hasCalendarPermission())
                    addFragment(CalendarPreferenceFragment())
            }
        )
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.isNotEmpty()) {
            super.onBackPressed()
            supportActionBar?.setTitle(R.string.configuration_activity_004)
            return
        }

        if (PermissionChecker.dismissSnackbar())
            return

        val preferenceChanged = Preference.getPreferenceChanged(this)
        val intent = Intent().apply {
            putExtra(Name.PreferenceChanged, preferenceChanged)
        }

        setResult(RESULT_OK, intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left)
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .add(R.id.fragment_container_view, fragment, fragment.tag)
                .commit()
    }
}