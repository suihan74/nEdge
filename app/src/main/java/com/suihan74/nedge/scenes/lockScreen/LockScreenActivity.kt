package com.suihan74.nedge.scenes.lockScreen

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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.database.notification.NotificationEntity
import com.suihan74.nedge.databinding.ActivityLockScreenBinding
import com.suihan74.utilities.exception.TaskFailureException
import com.suihan74.utilities.extensions.whenTrue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LockScreenActivity : AppCompatActivity() {
    companion object {
        /**
         * 可能なら`LockScreenActivity`に遷移する
         *
         * @see LockScreenViewModel.checkNotifiable
         */
        suspend fun startWhenAvailable(context: Context, sbn: StatusBarNotification?) {
            if (LockScreenViewModel.checkNotifiable(sbn)) {
                runCatching {
                    val intent = Intent(context, LockScreenActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    ContextCompat.startActivity(context, intent, null)
                }.onSuccess {
                    Log.i("Notification", sbn!!.packageName)
                }.onFailure {
                    Log.e("Notification", "failed to start LockScreenActivity:" + sbn!!.packageName)
                    FirebaseCrashlytics.getInstance().recordException(it)
                }
            }
        }

        /**
         * 通知が存在する場合`LockScreenActivity`を(再作成して)遷移する
         *
         * 使用禁止終了時刻での再起動に使用
         *
         * @throws TaskFailureException::class
         */
        suspend fun start(app: Application) : Boolean =
            app.notificationRepository.statusBarNotifications.value?.let { sbnList ->
                LockScreenViewModel.checkNotifiable(sbnList).whenTrue {
                    val intent = Intent(app, LockScreenActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    app.startActivity(intent)
                }
            } ?: false

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

    private val viewModel by viewModels<LockScreenViewModel>()

    private lateinit var binding : ActivityLockScreenBinding

    // ------ //

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.init(this@LockScreenActivity, intent)
        }

        binding = DataBindingUtil.setContentView<ActivityLockScreenBinding>(
                this,
                R.layout.activity_lock_screen
        ).also {
            it.vm = viewModel
            it.lifecycleOwner = this
        }
        overlapLockScreenAndKeepScreenOn(binding)

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
        lifecycleScope.launch {
            viewModel.onAttachedToWindow(this@LockScreenActivity, window, binding.notificationBar)
        }
    }

    /** 常に画面を点灯する設定 */
    private fun keepScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            keyguardManager?.requestDismissKeyguard(this, null)
        }
        else {
            @Suppress("deprecation")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * 常にフルスクリーンでロック画面より上に表示し，画面を完全に消灯しない
     */
    private fun overlapLockScreenAndKeepScreenOn(binding: ActivityLockScreenBinding) {
        keepScreenOn()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, binding.root).let { controller ->
                controller.hide(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
                )
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        else {
            @Suppress("DEPRECATION")
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
    }

    private fun activateVolumeControl() {
    }

    /** 戻るボタンを無効化する */
    override fun onBackPressed() {
        // do nothing
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_MUTE -> false
            else -> true
        }
    }

    override fun finish() {
        super.finish()
        viewModel.onFinishActivity()
    }
}
