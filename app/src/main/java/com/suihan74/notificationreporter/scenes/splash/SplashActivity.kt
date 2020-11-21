package com.suihan74.notificationreporter.scenes.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity

class SplashActivity : AppCompatActivity() {
    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1
    }

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
        else {
            launchContentsActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (Settings.canDrawOverlays(this)) {
                    launchContentsActivity()
                }
                else {
                    finish()
                }
            }
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
