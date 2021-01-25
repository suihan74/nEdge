package com.suihan74.notificationreporter.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.suihan74.notificationreporter.Application

/**
 * 定時に起動確認する`Worker`
 */
class StartupConfirmationWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val result = runCatching {
            val app = Application.instance
            val notifications = app.notificationRepository.statusBarNotifications.value.orEmpty()
            if (notifications.isNotEmpty()) {
                Log.d("StartUp", "need to turn screen on")
            }
        }

        return if (result.isSuccess) Result.success()
        else Result.failure()
    }
}
