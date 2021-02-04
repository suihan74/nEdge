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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.database.notification.NotificationEntity
import com.suihan74.nedge.databinding.ActivityLockScreenBinding
import com.suihan74.utilities.exception.TaskFailureException
import com.suihan74.utilities.extensions.whenTrue
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.launch

class LockScreenActivity : AppCompatActivity() {
    companion object {
        /**
         * 可能なら`LockScreenActivity`に遷移する
         *
         * @see LockScreenViewModel.checkNotifiable
         */
        suspend fun startWhenAvailable(context: Context, sbn: StatusBarNotification?) {
            if (LockScreenViewModel.checkNotifiable(sbn)) {
                val intent = Intent(context, LockScreenActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.i("Notification", sbn!!.packageName)
            }
        }

        /**
         * 通知が存在する場合`LockScreenActivity`を(再作成して)遷移する
         *
         * 使用禁止終了時刻での再起動に使用
         *
         * @throws TaskFailureException::class
         */
        fun start(app: Application) : Boolean {
            return try {
                app.notificationRepository.statusBarNotifications.value.isNullOrEmpty().not().whenTrue {
                    val intent = Intent(app, LockScreenActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    app.startActivity(intent)
                }
            }
            catch (e: Throwable) {
                throw TaskFailureException(cause = e)
            }
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
        overlapLockScreenAndKeepScreenOn()

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
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
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
    private fun overlapLockScreenAndKeepScreenOn() {
        keepScreenOn()

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
