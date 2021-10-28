package com.suihan74.nedge.scenes.preferences.dialog

import android.app.Dialog
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.suihan74.nedge.R
import com.suihan74.nedge.database.notification.BlackListEntity
import com.suihan74.nedge.databinding.DialogBlackListItemEditorBinding
import com.suihan74.nedge.models.KeywordMatchingType
import com.suihan74.nedge.repositories.PreferencesRepository
import com.suihan74.nedge.scenes.preferences.page.BlackListItem
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ブラックリスト項目を編集するダイアログ
 */
class BlackListItemEditorDialogFragment : DialogFragment() {

    companion object {
        fun createInstance(item: BlackListItem, repository: PreferencesRepository) = BlackListItemEditorDialogFragment().also {
            it.lifecycleScope.launchWhenCreated {
                it.viewModel.initialize(item, repository)
            }
        }
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        DialogViewModel()
    }

    // ------ //

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogBlackListItemEditorBinding.inflate(layoutInflater, null, false).also {
            it.vm = viewModel
            it.lifecycleOwner = this
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.prefs_black_list_item_editor_title)
            .setView(binding.root)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_ok) { _, _ -> viewModel.complete(this) }
            .create()
    }

    // ------ //

    class DialogViewModel : ViewModel() {
        private var prefsRepo : PreferencesRepository? = null

        /**
         * 編集対象のエンティティ
         *
         * 既存ならIDが入っているので、保存時にはこれをコピーして更新する
         */
        private var entity : BlackListEntity? = null

        /**
         * 対象アプリ情報
         */
        val applicationInfo = MutableLiveData<ApplicationInfo>()

        /**
         * キーワード
         */
        val keyword = MutableLiveData<String>()

        /**
         * キーワードマッチ方法
         */
        val keywordMatchingType = MutableLiveData<KeywordMatchingType>()

        // ------ //

        /**
         * 編集対象の情報をセットする
         */
        suspend fun initialize(item: BlackListItem, repository: PreferencesRepository) = withContext(Dispatchers.Main) {
            prefsRepo = repository
            entity = item.entity
            applicationInfo.value = item.appInfo
            keyword.value = item.entity.keyword
            keywordMatchingType.value = item.entity.keywordMatchingType
        }

        /**
         * 編集内容を保存してダイアログを閉じる
         */
        fun complete(dialog: BlackListItemEditorDialogFragment) = viewModelScope.launch(Dispatchers.Main) {
            try {
                val entity = entity!!.copy(
                    keyword = keyword.value!!,
                    keywordMatchingType = keywordMatchingType.value!!
                )
                prefsRepo!!.updateBlackListEntity(entity)
            }
            catch (e: Throwable) {
                Log.e("BlackListItemEditor", Log.getStackTraceString(e))
            }
            dialog.dismiss()
        }
    }
}
