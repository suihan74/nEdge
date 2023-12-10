package com.suihan74.nedge.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.suihan74.nedge.Application
import com.suihan74.nedge.scenes.lockScreen.LockScreenActivity
import kotlinx.coroutines.runBlocking

/**
 * 定時に起動確認する`Worker`
 */
class StartupConfirmationWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result = runBlocking {
        val result = runCatching {
            if (LockScreenActivity.start(Application.instance)) {
                Log.d("StartUp", "turn LockScreenActivity on")
            }
        }

        if (result.isSuccess) Result.success()
        else Result.failure()
    }
}
