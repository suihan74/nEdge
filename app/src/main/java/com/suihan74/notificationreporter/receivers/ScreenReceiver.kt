package com.suihan74.notificationreporter.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.suihan74.notificationreporter.Application
import kotlinx.coroutines.launch

/**
 * 画面ON/OFFを検知するレシーバ
 */
class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        setScreenState(Application.instance, intent)
    }

    private fun setScreenState(app: Application, intent: Intent?) = app.coroutineScope.launch {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                app.screenRepository.setScreenState(false)

                // 画面ON中にスタックに追加されていた通知をどうするか
                // TODO: 現段階では暫定的に「無視する」
                app.notificationRepository.clearNotifications()

                Log.i("ScreenReceiver", "detected screen off")
            }

            Intent.ACTION_SCREEN_ON -> {
                app.screenRepository.setScreenState(true)
                Log.i("ScreenReceiver", "detected screen on")
            }
        }
    }
}
