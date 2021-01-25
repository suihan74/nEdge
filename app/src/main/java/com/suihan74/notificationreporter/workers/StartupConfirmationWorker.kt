package com.suihan74.notificationreporter.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity

/**
 * 定時に起動確認する`Worker`
 */
class StartupConfirmationWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val result = runCatching {
            if (LockScreenActivity.start(Application.instance)) {
                Log.d("StartUp", "turn LockScreenActivity on")
            }
        }

        return if (result.isSuccess) Result.success()
        else Result.failure()
    }
}
