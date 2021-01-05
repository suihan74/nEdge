package com.suihan74.notificationreporter.scenes.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.utilities.extensions.whenFalse

/**
 * 起動時アクティビティ
 *
 * 必要なパーミッションが有効かのチェックを行い、完了後に設定画面に遷移する
 */
class SplashActivity : AppCompatActivity() {
    /** ユーザーにパーミッションを手動で許可させる処理の合否を受け取るためのリクエストコード */
    enum class RequestCode {
        /** 画面の最前面に表示する */
        OVERLAY_PERMISSION,

        /** 他のアプリが発した通知を取得する */
        NOTIFICATION_LISTENER
    }

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (requestNotificationListenerPermission() && requestManageOverlayPermission()) {
            launchContentsActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestNotificationListenerPermission() && requestManageOverlayPermission()) {
            launchContentsActivity()
        }
    }

    // ------ //

    /**
     * 画面最前面(ロック画面よりも上)に表示するためのパーミッション要求
     */
    private fun requestManageOverlayPermission() : Boolean {
        return Settings.canDrawOverlays(this).whenFalse {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, RequestCode.OVERLAY_PERMISSION.ordinal)
        }
    }

    /**
     * 他のアプリが発した通知を取得するためのパーミッション要求
     */
    private fun requestNotificationListenerPermission() : Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName).whenFalse {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivityForResult(intent, RequestCode.NOTIFICATION_LISTENER.ordinal)
        }
    }

    // ------ //

    /** アプリコンテンツ本体に遷移する */
    private fun launchContentsActivity() {
        val intent = Intent(
            this,
            PreferencesActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
