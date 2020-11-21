package com.suihan74.notificationreporter.scenes.preferences

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.ActivityPreferencesBinding
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity

/**
 * 設定画面
 */
class PreferencesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityPreferencesBinding>(
            this,
            R.layout.activity_preferences
        )

        binding.previewButton.setOnClickListener {
            val intent = Intent(this, LockScreenActivity::class.java)
            startActivity(intent)
        }
    }
}
