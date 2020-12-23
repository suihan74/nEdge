package com.suihan74.notificationreporter.scenes.preferences

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.databinding.ActivityPreferencesBinding
import com.suihan74.notificationreporter.models.NotchSetting
import com.suihan74.notificationreporter.models.NotchType
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity
import com.suihan74.notificationreporter.scenes.preferences.notch.RectangleNotchSettingFragment
import com.suihan74.notificationreporter.scenes.preferences.notch.WaterDropNotchSettingFragment
import com.suihan74.utilities.fragment.AlertDialogFragment
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 設定画面
 */
class PreferencesActivity : AppCompatActivity() {

    val viewModel by lazyProvideViewModel {
        val app = Application.instance
        PreferencesViewModel(app.preferencesRepository)
    }

    private lateinit var binding: ActivityPreferencesBinding

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.PreferencesActivity)

        // 全画面表示する
        window.decorView.let { decorView ->
            val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            decorView.systemUiVisibility = flags

            decorView.setOnSystemUiVisibilityChangeListener {
                decorView.systemUiVisibility = flags
            }
        }

        binding = DataBindingUtil.setContentView<ActivityPreferencesBinding>(
            this,
            R.layout.activity_preferences
        ).also {
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

        binding.notchTypeSelectionButton.setOnClickListener {
            val dialog = AlertDialogFragment.Builder()
                .setTitle(R.string.prefs_notch_type_selection_desc)
                .setItems(NotchType.values().map { it.name }) { _, which ->
                    viewModel.topNotchSetting.value = NotchSetting.createInstance(type = NotchType.values()[which])
                    viewModel.topNotchType.value = NotchType.values()[which]
                }
                .setNegativeButton(R.string.dialog_cancel)
                .create()
            dialog.show(supportFragmentManager, null)
        }

        viewModel.topNotchType.observe(this, {
            val fragment = when (it) {
                NotchType.RECTANGLE ->
                    RectangleNotchSettingFragment.createInstance(NotificationEntity.DEFAULT_SETTING_NAME)

                NotchType.WATER_DROP ->
                    WaterDropNotchSettingFragment.createInstance(NotificationEntity.DEFAULT_SETTING_NAME)

                else -> Fragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.notchSettingFragmentArea, fragment)
                .commit()
        })

        // TODO: インストール済みアプリ一覧の取得
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        resolveInfoList.forEach {
            Log.d("pm", "packageName: " + it.activityInfo.applicationInfo.name)
            Log.d("pm", "---")

        }
        /*
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        packages.forEach {
            // it.name == null
            // it.icon == null
            Log.d("pm", "packageName: " + it.packageName)
            Log.d("pm", "---")
        }
         */
    }

    // スクリーン輪郭線・ノッチ輪郭線の描画がウィンドウアタッチ後でないとできないため
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.vm = viewModel
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveSettings()
    }

    // ------ //

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
