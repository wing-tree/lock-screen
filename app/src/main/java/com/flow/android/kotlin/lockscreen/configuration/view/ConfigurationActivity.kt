package com.flow.android.kotlin.lockscreen.configuration.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.configuration.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.configuration.calendar.view.CalendarConfigurationFragment
import com.flow.android.kotlin.lockscreen.configuration.display.view.DisplayConfigurationFragment
import com.flow.android.kotlin.lockscreen.configuration.lockscreen.view.LockScreenConfigurationFragment
import com.flow.android.kotlin.lockscreen.databinding.ActivityConfigurationBinding
import com.flow.android.kotlin.lockscreen.util.BLANK
import java.security.Key

class ConfigurationActivity: AppCompatActivity() {
    private var _viewBinding: ActivityConfigurationBinding? = null
    private val viewBinding: ActivityConfigurationBinding
        get() = _viewBinding!!

    private val activity = this
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())
    //private val viewModel: MainViewModel by activityViewModels() // todo. 그냥 preferences에서 받아올 것. 액트 종료시, 맞춰 업뎃.

    object ConfigurationChanged {
        const val Key = "com.flow.android.kotlin.lockscreen.configuration.view" +
                ".ConfigurationActivity.ConfigurationChanged.Key"

        const val Calendar = 211
    }

    private val configurationAdapter: ConfigurationAdapter by lazy {
        ConfigurationAdapter(arrayListOf(
                AdapterItem.Item(
                        drawable = null,
                        description = BLANK,
                        onClick = { _, _ ->
                            addFragment(CalendarConfigurationFragment())
                        },
                        title = getString(R.string.calendar)
                ),
                AdapterItem.Item(
                        drawable = null,
                        description = getString(R.string.configuration_activity_002),
                        onClick = { _, _ ->
                            addFragment(DisplayConfigurationFragment())
                        },
                        title = getString(R.string.configuration_activity_001)
                ),
                AdapterItem.Item(
                        drawable = null,
                        description = getString(R.string.configuration_activity_000),
                        onClick = { _, _ ->
                            addFragment(LockScreenConfigurationFragment())
                        },
                        title = getString(R.string.configuration_activity_003)
                )
        ))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewBinding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.recyclerView.apply {
            adapter = configurationAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        val uncheckedCalendarIds = ConfigurationPreferences.getUncheckedCalendarIds(this)
        val checkBoxItems = CalendarHelper.calendarDisplays(contentResolver).map {
            CheckBoxItem(
                    isChecked = uncheckedCalendarIds.contains(it.id.toString()).not(),
                    text = it.name,
                    onCheckedChange = { isChecked ->
                        if (isChecked)
                            ConfigurationPreferences.removeUncheckedCalendarId(this, it.id.toString())
                        else
                            ConfigurationPreferences.addUncheckedCalendarId(this, it.id.toString())
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
            putExtra(ConfigurationChanged.Key, ConfigurationChanged.Calendar)
        }

        setResult(RESULT_OK, intent)
        finish()
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