package com.suihan74.notificationreporter.scenes.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.utilities.extensions.onNot
import com.suihan74.utilities.extensions.whenFalse

class SplashActivity : AppCompatActivity() {
    enum class RequestCode {
        OVERLAY_PERMISSION,
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

        when (requestCode) {
            RequestCode.OVERLAY_PERMISSION.ordinal -> requestManageOverlayPermission()

            RequestCode.NOTIFICATION_LISTENER.ordinal -> requestNotificationListenerPermission()
        }
    }

    // ------ //

    private fun requestManageOverlayPermission() : Boolean {
        return Settings.canDrawOverlays(this).whenFalse {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, RequestCode.OVERLAY_PERMISSION.ordinal)
        }
    }

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
        )
        startActivity(intent)
    }
}
