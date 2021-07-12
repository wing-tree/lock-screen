package com.flow.android.kotlin.lockscreen.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseActivity
import com.flow.android.kotlin.lockscreen.databinding.ActivitySettingsBinding

class SettingsTestActivity: BaseActivity() {
    private val viewBinding by lazy { ActivitySettingsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingFragment())
            .commit()
    }

    class SettingFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.settings)
        }
    }
}