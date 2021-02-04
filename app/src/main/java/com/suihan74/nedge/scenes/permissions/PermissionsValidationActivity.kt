package com.suihan74.nedge.scenes.permissions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.suihan74.nedge.databinding.ActivityPermissionsValidationBinding
import com.suihan74.nedge.scenes.preferences.PreferencesActivity
import com.suihan74.utilities.lazyProvideViewModel

/**
 * 起動時パーミッションチェック用アクティビティ
 */
class PermissionsValidationActivity : AppCompatActivity() {
    companion object {
        /**
         * アプリの実行に必要なすべてのパーミッションが許可されているか確認する
         *
         * @return true: 全てのパーミッションが許可されている
         */
        fun allPermissionsAllowed(context: Context): Boolean =
            PermissionsValidationViewModel.allPermissionsAllowed(context)
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        PermissionsValidationViewModel()
    }

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (allPermissionsAllowed(this)) {
            launchContentsActivity()
        }
        else {
            val binding = ActivityPermissionsValidationBinding.inflate(layoutInflater).also {
                it.vm = viewModel
                it.activity = this
                it.lifecycleOwner = this
            }
            setContentView(binding.root)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshStates(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (allPermissionsAllowed(this)) {
            launchContentsActivity()
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
}
