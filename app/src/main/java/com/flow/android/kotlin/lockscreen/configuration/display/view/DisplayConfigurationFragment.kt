package com.flow.android.kotlin.lockscreen.configuration.display.view

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.configuration.adapter.AdapterItem
import com.flow.android.kotlin.lockscreen.configuration.adapter.ConfigurationAdapter
import com.flow.android.kotlin.lockscreen.databinding.ItemBinding
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisplayConfigurationFragment : ConfigurationFragment() {
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        publishSubject.onNext(isGranted)
    }

    private val publishSubject = PublishSubject.create<Boolean>()
    private var disposable: Disposable? = null

    override fun onResume() {
        super.onResume()

        disposable = publishSubject.subscribe { isGranted ->
            if (isGranted) {
                ConfigurationPreferences.putLockScreenTextColor(requireContext(), LockScreenTextColor.DependingOnWallpaper)
                configurationAdapter.updateDescription(LOCK_SCREEN_TEXT_COLOR_ID, lockScreenTextColors[2])
            } else {
                showToast("denied.")
            }
        }
    }

    override fun createConfigurationAdapter(): ConfigurationAdapter {
        val context = requireContext()

        return ConfigurationAdapter(arrayListOf(
                AdapterItem.SwitchItem(
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_dark_mode_24),
                        isChecked = ConfigurationPreferences.getDarkMode(context),
                        onCheckedChange = { isChecked ->
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    ConfigurationPreferences.putDarkMode(requireContext(), isChecked)
                                    delay(duration)
                                }

                                AppCompatDelegate.setDefaultNightMode(
                                        if (isChecked)
                                            AppCompatDelegate.MODE_NIGHT_YES
                                        else
                                            AppCompatDelegate.MODE_NIGHT_NO
                                )
                            }
                        },
                        title = getString(R.string.display_configuration_fragment_000)
                ),
                AdapterItem.Item(
                        id = LOCK_SCREEN_TEXT_COLOR_ID,
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_round_format_color_text_24),
                        description = lockScreenTextColors[ConfigurationPreferences.getLockScreenTextColor(requireContext())],
                        onClick = { itemBinding: ItemBinding, _ ->
                            MaterialAlertDialogBuilder(requireContext()).setItems(lockScreenTextColors) { _, i ->
                                when (i) {
                                    0 -> {
                                        ConfigurationPreferences.putLockScreenTextColor(requireContext(), LockScreenTextColor.Black)
                                        itemBinding.textDescription.text = lockScreenTextColors[0]
                                    }
                                    1 -> {
                                        ConfigurationPreferences.putLockScreenTextColor(requireContext(), LockScreenTextColor.White)
                                        itemBinding.textDescription.text = lockScreenTextColors[1]
                                    }
                                    2 -> {
                                        activityResultLauncher.launch(READ_EXTERNAL_STORAGE)
                                    }
                                }
                            }.show()
                        },
                        title = getString(R.string.display_configuration_fragment_001)
                ),
        ))
    }

    private val duration = 300L

    object LockScreenTextColor {
        const val Black = 0
        const val White = 1
        const val DependingOnWallpaper = 2
    }

    private val lockScreenTextColors by lazy {
        requireContext().resources.getStringArray(R.array.display_configuration_fragment_002)
    }

    companion object {
        private const val LOCK_SCREEN_TEXT_COLOR_ID = 2055L
    }
}