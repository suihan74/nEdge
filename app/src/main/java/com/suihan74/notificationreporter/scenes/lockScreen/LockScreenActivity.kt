package com.suihan74.notificationreporter.scenes.lockScreen

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.databinding.ActivityLockScreenBinding
import com.suihan74.notificationreporter.models.UnknownNotificationSolution
import com.suihan74.utilities.extensions.between
import com.suihan74.utilities.lazyProvideViewModel
import org.threeten.bp.LocalTime

class LockScreenActivity : AppCompatActivity() {
    companion object {
        /**
         * 可能なら`LockScreenActivity`に遷移する
         *
         * @see checkNotifiable
         */
        suspend fun startWhenAvailable(context: Context, sbn: StatusBarNotification?) {
            if (checkNotifiable(sbn)) {
                val intent = Intent(context, LockScreenActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.i("Notification", sbn!!.packageName)
            }
        }

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

            val prefs = prefRepo.preferences()

            // バッテリレベルが指定値未満
            val batteryLevel = batteryRepo.batteryLevel.value ?: 0
            val requiredBatteryLevel = prefs.requiredBatteryLevel
            if (batteryLevel < requiredBatteryLevel && batteryRepo.batteryCharging.value != true) {
                return false
            }

            // 通知を行わない時間帯
            if (LocalTime.now().between(prefs.silentTimezoneStart, prefs.silentTimezoneEnd)) {
                return false
            }

            // 無視する通知
            if (prefs.unknownNotificationSolution == UnknownNotificationSolution.IGNORE) {
                if (null == prefRepo.getNotificationSettingOrNull(sbn)) {
                    return false
                }
            }

            return true
        }

        /** プレビュー用に`LockScreenActivity`を開く */
        fun startPreview(context: Context, entity: NotificationEntity) {
            val intent = Intent(context, LockScreenActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                it.putExtra(LockScreenViewModel.Extra.PREVIEW_ENTITY_ID.name, entity.id)
            }
            context.startActivity(intent)
        }
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        LockScreenViewModel(Application.instance)
    }

    private lateinit var binding : ActivityLockScreenBinding

    // ------ //

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.LockScreenActivity)
        overlapLockScreenAndKeepScreenOn()

        viewModel.init(this, intent)

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
    }

    // ノッチ情報の取得はウィンドウアタッチ後でないとできない
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // バックライトの制御
        viewModel.observeScreenBrightness(this, window)

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
        viewModel.onFinishActivity()
    }
}
