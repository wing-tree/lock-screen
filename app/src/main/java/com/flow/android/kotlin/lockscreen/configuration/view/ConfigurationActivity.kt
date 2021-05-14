package com.flow.android.kotlin.lockscreen.configuration.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.configuration.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxAdapter
import com.flow.android.kotlin.lockscreen.configuration.adapter.CheckBoxItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.databinding.ActivityConfigurationBinding
import com.flow.android.kotlin.lockscreen.lockscreen.LockScreenService

class ConfigurationActivity: AppCompatActivity() {
    private var _viewBinding: ActivityConfigurationBinding? = null
    private val viewBinding: ActivityConfigurationBinding
        get() = _viewBinding!!

    private val activity = this
    private val checkBoxAdapter = CheckBoxAdapter(arrayListOf())
    //private val viewModel: MainViewModel by activityViewModels() // todo. 그냥 preferences에서 받아올 것. 액트 종료시, 맞춰 업뎃.

    private object Id {
        const val DisplayAfterUnlocking = 2249L
    }

    private val configurationAdapter: ConfigurationAdapter by lazy {
        ConfigurationAdapter(arrayListOf(
                AdapterItem.SwitchItem(
                        drawable = getDrawable(R.drawable.ic_round_lock_24),
                        isChecked = ConfigurationPreferences.getShowOnLockScreen(this),
                        onCheckedChange = { isChecked ->
                            ConfigurationPreferences.putShowOnLockScreen(this, isChecked)

                            if (isChecked) {
                                configurationAdapter.showItem(Id.DisplayAfterUnlocking)

                                val intent = Intent(this, LockScreenService::class.java)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    startForegroundService(intent)
                                else
                                    startService(intent)
                            } else {
                                configurationAdapter.hideItem(Id.DisplayAfterUnlocking)
                                sendBroadcast(Intent(LockScreenService.Action.StopSelf))
                            }
                        },
                        title = getString(R.string.show_on_lock_screen)
                ),
                AdapterItem.SwitchItem(
                        drawable = null,
                        id = Id.DisplayAfterUnlocking,
                        isChecked = ConfigurationPreferences.getDisplayAfterUnlocking(this),
                        onCheckedChange = { isChecked ->
                            ConfigurationPreferences.putDisplayAfterUnlocking(this, isChecked)
                        },
                        title = getString(R.string.display_after_unlocking)
                ),
                AdapterItem.SubtitleItem (
                        subtitle = getString(R.string.calendar)
                ),
                AdapterItem.ListItem(
                        adapter = checkBoxAdapter,
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_round_today_24),
                        onClick = { listItemBinding, listItem ->

                        },
                        title = getString(R.string.calendar),
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
}