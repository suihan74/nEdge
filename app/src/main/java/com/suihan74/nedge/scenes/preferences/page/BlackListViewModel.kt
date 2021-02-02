package com.suihan74.nedge.scenes.preferences.page

import android.content.pm.ApplicationInfo
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.database.notification.BlackListEntity
import com.suihan74.nedge.scenes.preferences.dialog.ApplicationSelectionDialogFragment
import com.suihan74.nedge.scenes.preferences.dialog.BlackListItemEditorDialogFragment
import com.suihan74.utilities.fragment.AlertDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BlackListViewModel(
    private val application : Application
) : ViewModel() {
    /**
     * 通知表示設定リスト
     */
    val settings = MutableLiveData<List<BlackListItem>>()

    // ------ //

    init {
        application.preferencesRepository.allBlackListEntitiesFlow
            .onEach {
                settings.postValue(
                    it.map { entity -> blackListItem(entity) }
                )
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    // ------ //

    private val packageManager by lazy { application.packageManager }

    private fun blackListItem(entity: BlackListEntity) : BlackListItem {
        val appInfo = packageManager.getApplicationInfo(entity.packageName, 0)
        return BlackListItem(
            appName = packageManager.getApplicationLabel(appInfo).toString(),
            appInfo = appInfo,
            entity = entity
        )
    }

    // ------ //

    /** 項目に対するメニューを表示 */
    fun openBlackListItemMenuDialog(item: BlackListItem, fragmentManager: FragmentManager) {
        val items = listOf<Pair<Int, (AlertDialogFragment)->Unit>>(
            R.string.prefs_settings_list_item_menu_edit to { f ->
                openBlackListItemEditorDialog(item, f.parentFragmentManager)
            },
            R.string.prefs_settings_list_item_menu_delete to {
                viewModelScope.launch(Dispatchers.Default) {
                    application.preferencesRepository.deleteBlackListEntity(item.entity)
                    settings.postValue(
                        settings.value!!.filterNot { it == item }
                    )
                }
            },
        )

        val dialog = AlertDialogFragment.Builder()
            .setTitle(item.appName)
            .setNegativeButton(R.string.dialog_cancel)
            .setItems(items.map { it.first }) { f, which ->
                items[which].second(f)
            }
            .create()

        dialog.show(fragmentManager, null)
    }

    /** ブラックリスト項目を編集するダイアログを開く */
    fun openBlackListItemEditorDialog(item: BlackListItem, fragmentManager: FragmentManager) {
        val dialog = BlackListItemEditorDialogFragment.createInstance(item, application.preferencesRepository)
        dialog.show(fragmentManager, null)
    }

    /**
     * ブラックリスト項目を新規作成開始する
     *
     * 編集対象アプリを選択し，設定エディタを開く
     */
    fun createNewBlackListItem(fragmentManager: FragmentManager) {
        val dialog = ApplicationSelectionDialogFragment.createInstance { f, item ->
            val blackListItem = BlackListItem(
                appName = item.appName,
                appInfo = item.applicationInfo,
                entity = BlackListEntity(item.applicationInfo.packageName)
            )
            openBlackListItemEditorDialog(blackListItem, f.parentFragmentManager)
        }
        dialog.show(fragmentManager, null)
    }
}

// ------ //

/**
 * アプリ情報を反映した表示用データ
 */
data class BlackListItem(
    val appName : String,
    val appInfo : ApplicationInfo,
    val entity : BlackListEntity
) {
    class DiffCallback : DiffUtil.ItemCallback<BlackListItem>() {
        override fun areItemsTheSame(oldItem: BlackListItem, newItem: BlackListItem) =
            oldItem.entity.id == newItem.entity.id

        override fun areContentsTheSame(oldItem: BlackListItem, newItem: BlackListItem) =
            oldItem.appName == newItem.appName &&
                    oldItem.entity.keyword == newItem.entity.keyword &&
                    oldItem.entity.keywordMatchingType == newItem.entity.keywordMatchingType
    }
}
