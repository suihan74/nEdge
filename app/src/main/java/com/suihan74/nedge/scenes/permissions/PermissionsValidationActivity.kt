package com.suihan74.nedge.scenes.permissions

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.suihan74.nedge.databinding.ActivityPermissionsValidationBinding
import com.suihan74.utilities.lazyProvideViewModel

/**
 * 起動時パーミッションチェック用アクティビティ
 */
class PermissionsValidationActivity : AppCompatActivity() {
    companion object {
        /**
         * 起動時の自動実行であるかのフラグ
         *
         * extra value type: boolean
         */
        const val EXTRA_BOOTSTRAP = "EXTRA_BOOTSTRAP"

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
        val launchAsBootstrap = intent.getBooleanExtra(EXTRA_BOOTSTRAP, false)

        viewModel.onCreateActivity(this, launchAsBootstrap)

        if (launchAsBootstrap && allPermissionsAllowed(this)) {
            viewModel.launchContentsActivity(this)
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
}
