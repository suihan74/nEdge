package com.suihan74.notificationreporter.scenes.preferences

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.ActivityPreferencesBinding
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 設定画面
 */
class PreferencesActivity : AppCompatActivity() {

    private val viewModel by lazyProvideViewModel {
        val app = Application.instance
        PreferencesViewModel(app.preferencesRepository)
    }

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityPreferencesBinding>(
            this,
            R.layout.activity_preferences
        ).also {
            it.vm = viewModel
            it.lifecycleOwner = this
        }

        binding.previewButton.setOnClickListener {
            val intent = Intent(this, LockScreenActivity::class.java)
            startActivity(intent)
        }

        binding.notifyButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(5_000L)
                notifyDummy()
            }
        }
    }

    /** ダミーの通知を発生させる */
    private fun notifyDummy() {
        val channelId = "DummyNotificationChannel"
        val notificationId = 334

        createNotificationChannel(channelId)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("dummy")
            .setContentText("dummy")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    /** 通知チャンネルを作成する */
    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val name = "dummy"
        val description = "dummy notifications for test"
        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT).also {
            it.description = description
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
