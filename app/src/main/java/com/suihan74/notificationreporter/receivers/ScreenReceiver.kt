package com.suihan74.notificationreporter.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import com.suihan74.notificationreporter.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 画面ON/OFFを検知するレシーバ
 */
class ScreenReceiver : BroadcastReceiver() {
    @MainThread
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val app = Application.instance

        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                app.screenRepository.screenOn.value = false

                // 画面ON中にスタックに追加された通知の対応
                // TODO: 現段階では暫定的に「無視する」
                GlobalScope.launch {
                    app.notificationRepository.clearNotifications()
                }

                Log.i("ScreenReceiver", "detected screen off")
            }

            Intent.ACTION_SCREEN_ON -> {
                app.screenRepository.screenOn.value = true
                Log.i("ScreenReceiver", "detected screen on")
            }
        }
    }
}
