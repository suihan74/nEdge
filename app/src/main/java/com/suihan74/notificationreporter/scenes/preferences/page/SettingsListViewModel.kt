package com.suihan74.notificationreporter.scenes.preferences.page

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.database.notification.isDefault
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.notificationreporter.scenes.preferences.dialog.ApplicationSelectionDialogFragment
import com.suihan74.utilities.extensions.alsoAs
import com.suihan74.utilities.fragment.AlertDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsListViewModel(
    private val application : Application
) : ViewModel() {
    /**
     * 通知表示設定リスト
     */
    val settings = MutableLiveData<List<SettingItem>>()

    /**
     * デフォルト設定
     */
    var defaultSettingEntity: NotificationEntity? = null

    // ------ //

    init {
        application.preferencesRepository.allNotificationEntitiesFlow
            .onEach {
                defaultSettingEntity =
                    it.firstOrNull { entity -> entity.isDefault }
                        ?: application.preferencesRepository.getDefaultNotificationEntity()

                settings.postValue(
                    it.filterNot { entity -> entity == defaultSettingEntity }
                        .map { entity -> settingItem(entity) }
                )
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    // ------ //

    private val packageManager by lazy { application.packageManager }

    private fun settingItem(entity: NotificationEntity) : SettingItem {
        val appInfo = packageManager.getApplicationInfo(entity.packageName, PackageManager.GET_META_DATA)
        return SettingItem(
            appName = packageManager.getApplicationLabel(appInfo).toString(),
            appInfo = appInfo,
            entity = entity
        )
    }

    // ------ //

    /** 設定項目に対するメニューを表示 */
    fun openSettingItemMenuDialog(item: SettingItem, fragmentManager: FragmentManager) {
        val items = listOf<Pair<Int, (AlertDialogFragment)->Unit>>(
            R.string.prefs_settings_list_item_menu_edit to { f ->
                f.activity.alsoAs<PreferencesActivity> {
                    it.openSettingEditor(item.entity)
                }
            },
            R.string.prefs_settings_list_item_menu_preview to {
                LockScreenActivity.startPreview(application, item.entity)
            },
            R.string.prefs_settings_list_item_menu_copy to { f ->
                createNewNotificationSetting(f.parentFragmentManager, item.entity.setting)
            },
            R.string.prefs_settings_list_item_menu_delete to {
                viewModelScope.launch(Dispatchers.Default) {
                    application.preferencesRepository.deleteNotificationEntity(item.entity)
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

    /**
     * 通知表示設定を新規作成開始する
     *
     * 編集対象アプリを選択し，通知設定エディタを開く
     */
    fun createNewNotificationSetting(fragmentManager: FragmentManager, setting: NotificationSetting? = null) {
        val dialog = ApplicationSelectionDialogFragment.createInstance { f, item ->
            val entity = NotificationEntity(
                packageName = item.applicationInfo.packageName,
                displayName = item.appName,
                setting = setting ?: defaultSettingEntity?.setting ?: NotificationSetting()
            )
            f.activity.alsoAs<PreferencesActivity> { activity ->
                activity.openSettingEditor(entity)
            }
        }
        dialog.show(fragmentManager, null)
    }
}

// ------ //

/**
 * アプリ情報を反映した表示用データ
 */
data class SettingItem(
    val appName : String,
    val appInfo : ApplicationInfo,
    val entity : NotificationEntity
) {
    class DiffCallback : DiffUtil.ItemCallback<SettingItem>() {
        override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem) =
            oldItem.appName == newItem.appName

        override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem) =
            oldItem.entity.keyword == newItem.entity.keyword &&
                    oldItem.entity.keywordMatchingType == newItem.entity.keywordMatchingType &&
                    oldItem.entity.displayName == newItem.entity.displayName
    }
}
