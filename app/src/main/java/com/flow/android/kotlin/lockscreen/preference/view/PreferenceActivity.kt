package com.flow.android.kotlin.lockscreen.preference.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.preference.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.preference.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.preference.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.preference.adapter.PreferenceAdapter
import com.flow.android.kotlin.lockscreen.preference.calendar.view.CalendarPreferenceFragment
import com.flow.android.kotlin.lockscreen.preference.display.view.DisplayPreferenceFragment
import com.flow.android.kotlin.lockscreen.preference.lockscreen.view.LockScreenPreferenceFragment
import com.flow.android.kotlin.lockscreen.preference.viewmodel.PreferenceViewModel
import com.flow.android.kotlin.lockscreen.databinding.ActivityConfigurationBinding

class PreferenceActivity: AppCompatActivity() {
    private val viewBinding by lazy { ActivityConfigurationBinding.inflate(layoutInflater) }
    private val viewModel: PreferenceViewModel by viewModels()

    private val activity = this
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())

    private val adapter: PreferenceAdapter by lazy {
        PreferenceAdapter(arrayListOf(
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_round_today_24),
                        description = getString(R.string.configuration_activity_006),
                        onClick = { _, _ ->
                            addFragment(CalendarPreferenceFragment())
                        },
                        title = getString(R.string.calendar)
                ),
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_mobile_48px),
                        description = getString(R.string.configuration_activity_002),
                        onClick = { _, _ ->
                            addFragment(DisplayPreferenceFragment())
                        },
                        title = getString(R.string.configuration_activity_001)
                ),
                AdapterItem.Preference(
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_round_screen_lock_portrait_24),
                        description = getString(R.string.configuration_activity_000),
                        onClick = { _, _ ->
                            addFragment(LockScreenPreferenceFragment())
                        },
                        title = getString(R.string.configuration_activity_003)
                )
        ))
    }

    object Name {
        private const val Prefix = "com.flow.android.kotlin.lockscreen.configuration.view" +
                ".ConfigurationActivity.Name"
        const val ConfigurationChange = "$Prefix.ConfigurationChange"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initializeToolbar(viewBinding.toolbar)

        viewBinding.recyclerView.apply {
            adapter = this@PreferenceActivity.adapter
            layoutManager = LinearLayoutManager(activity)
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
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.isNotEmpty()) {
            super.onBackPressed()
            return
        }

        val intent = Intent().apply {
            putExtra(Name.ConfigurationChange, viewModel.configurationChanged)
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