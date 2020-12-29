package com.suihan74.notificationreporter.scenes.lockScreen

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.dataStore.PreferencesKey
import com.suihan74.notificationreporter.databinding.ActivityLockScreenBinding
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalTime

class LockScreenActivity : AppCompatActivity() {
    companion object {
        /**
         * `LockScreenActivity`に遷移するべきかをチェックする
         *
         * 遷移を拒否する条件
         * - 通知が不正(=null)
         * - 画面が点灯中
         * - バッテリレベルが指定値未満
         * - 通知を行わない時間帯
         *
         * @return 遷移できる: true, 遷移できない: false
         */
        suspend fun checkNotifiable(sbn: StatusBarNotification?) : Boolean {
            val app = Application.instance
            val prefRepo = app.preferencesRepository
            val batteryRepo = app.batteryRepository
            val screenRepo = app.screenRepository

            // 通知が不正, 画面が点いている
            if (sbn?.notification == null || screenRepo.screenOn.value == true) {
                return false
            }

            // バッテリレベルが指定値未満
            val batteryLevel = batteryRepo.batteryLevel.value ?: 0
            val requiredBatteryLevel = prefRepo.getPreference(PreferencesKey.REQUIRED_BATTERY_LEVEL)
            if (batteryLevel < requiredBatteryLevel && batteryRepo.batteryCharging.value != true) {
                return false
            }

            // 通知を行わない時間帯
            val silentTimeZoneStart = prefRepo.getPreference(PreferencesKey.SILENT_TIMEZONE_START)
            val silentTimeZoneEnd = prefRepo.getPreference(PreferencesKey.SILENT_TIMEZONE_END)
            val now = LocalTime.now().toSecondOfDay()
            val considerDateChange = silentTimeZoneStart > silentTimeZoneEnd
            if (considerDateChange) {
                if (now >= silentTimeZoneStart || now <= silentTimeZoneEnd) {
                    return false
                }
            }
            else {
                if (now in silentTimeZoneStart..silentTimeZoneEnd) {
                    return false
                }
            }

            return true
        }
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        val app = Application.instance
        LockScreenViewModel(
            batteryRepo = app.batteryRepository,
            notificationRepo = app.notificationRepository,
            prefRepo = app.preferencesRepository
        )
    }

    private lateinit var binding : ActivityLockScreenBinding

    // ------ //

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.LockScreenActivity)
        overlapLockScreenAndKeepScreenOn()

        viewModel.init(this)

        binding = DataBindingUtil.setContentView<ActivityLockScreenBinding>(
                this,
                R.layout.activity_lock_screen
        ).also {
            it.vm = viewModel
            it.lifecycleOwner = this
        }

        // 画面上部にスワイプして画面を終了する
        binding.motionLayout.also {
            it.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    if (currentId == R.id.end) {
                        finish()
                    }
                }

                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                }

                override fun onTransitionTrigger(
                        p0: MotionLayout?,
                        p1: Int,
                        p2: Boolean,
                        p3: Float
                ) {
                }
            })
        }

        // バックライトを最低レベルにする
        viewModel.lightOff.observe(this, { lightOff ->
            window.attributes = window.attributes.also { lp ->
                lp.screenBrightness =
                        if (lightOff) 0.01f  // 1/256以下の値にするとバグる機種がある
                        else -1.0f  // システムの値
            }
        })
    }

    // ノッチ情報の取得はウィンドウアタッチ後でないとできない
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val notificationDrawer = NotificationDrawer(window)
        viewModel.notificationSetting.observe(this, {
            notificationDrawer.draw(binding.notificationBar, it)
        })
    }

    /**
     * 常にフルスクリーンでロック画面より上に表示し，画面を完全に消灯しない
     */
    private fun overlapLockScreenAndKeepScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        }
        else {
            @Suppress("deprecation")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
    }

    /** 戻るボタンを無効化する */
    override fun onBackPressed() {
        // do nothing
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return true
    }

    override fun finish() {
        super.finish()
        GlobalScope.launch {
            Application.instance.notificationRepository.clearNotifications()
        }
    }
}
