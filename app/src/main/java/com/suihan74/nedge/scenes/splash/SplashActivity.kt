package com.suihan74.nedge.scenes.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.suihan74.nedge.scenes.permissions.PermissionsValidationActivity
import com.suihan74.nedge.scenes.preferences.PreferencesActivity

/**
 * 起動時アクティビティ
 *
 * 必要なパーミッションが有効かのチェックを行い、完了後に設定画面に遷移する
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PermissionsValidationActivity.allPermissionsAllowed(this)) {
            launchContentsActivity()
        }
        else {
            launchPermissionsValidationActivity()
        }
    }

    // ------ //

    /** アプリコンテンツ本体に遷移する */
    private fun launchContentsActivity() {
        val intent = Intent(this, PreferencesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    /** パーミッション設定画面に遷移する */
    private fun launchPermissionsValidationActivity() {
        val intent = Intent(this, PermissionsValidationActivity::class.java).apply {
            putExtra(PermissionsValidationActivity.EXTRA_BOOTSTRAP, true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
