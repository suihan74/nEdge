package com.suihan74.notificationreporter.scenes.lockScreen

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.ActivityLockScreenBinding
import com.suihan74.utilities.provideViewModel

class LockScreenActivity : AppCompatActivity() {
    val viewModel by lazy {
        provideViewModel(this) {
            val app = Application.instance
            LockScreenViewModel(app.batteryRepository)
        }
    }

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlapLockScreenAndKeepScreenOn()

        val binding = DataBindingUtil.setContentView<ActivityLockScreenBinding>(
            this,
            R.layout.activity_lock_screen
        ).also {
            it.vm = viewModel
            it.lifecycleOwner = this
        }

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
                ) {}
            })
        }
    }

    /**
     * 常にロック画面より上に表示し，画面を消灯しない
     */
    private fun overlapLockScreenAndKeepScreenOn() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
        }
        else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
