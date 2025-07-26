package com.suihan74.nedge.scenes.preferences.page

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.database.notification.NotificationEntity
import com.suihan74.nedge.database.notification.isDefault
import com.suihan74.nedge.models.NotificationSetting
import com.suihan74.nedge.scenes.lockScreen.LockScreenActivity
import com.suihan74.nedge.scenes.preferences.PreferencesActivity
import com.suihan74.nedge.scenes.preferences.dialog.ApplicationSelectionDialogFragment
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
                        .mapNotNull { entity -> settingItem(entity) }
                )
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    // ------ //

    private val packageManager by lazy { application.packageManager }

    private fun settingItem(entity: NotificationEntity) : SettingItem? {
        val result = runCatching {
            val appInfo =
                packageManager.getApplicationInfo(entity.packageName, PackageManager.GET_META_DATA)
            SettingItem(
                appName = packageManager.getApplicationLabel(appInfo).toString(),
                appInfo = appInfo,
                entity = entity
            )
        }
        return result.getOrNull()
    }

    // ------ //

    fun openDefaultSettingMenuDialog(fragmentManager: FragmentManager) {
        val items = listOf<Pair<Int, (AlertDialogFragment)->Unit>>(
            R.string.pref_default_setting_menu_copy_shapes_to_items to { f ->
                AlertDialogFragment.Builder()
                    .setTitle(R.string.confirm_dialog_title)
                    .setMessage(R.string.prefs_settings_list_copy_shapes_description)
                    .setNegativeButton(R.string.dialog_cancel)
                    .setPositiveButton(R.string.dialog_ok) { f ->
                        viewModelScope.launch(Dispatchers.Main) {
                            runCatching {
                                val entities = settings.value.orEmpty().map { it.entity }
                                application.preferencesRepository.copyShapesFromDefault(entities)
                                Toast.makeText(
                                    application.applicationContext,
                                    R.string.prefs_settings_list_copy_shapes_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.onFailure {
                                Toast.makeText(
                                    application.applicationContext,
                                    R.string.prefs_settings_list_copy_shapes_failure,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .create()
                    .show(fragmentManager, null)
            }
        )

        AlertDialogFragment.Builder()
            .setTitle(R.string.prefs_settings_list_default_setting_text)
            .setNegativeButton(R.string.dialog_cancel)
            .setItems(items.map { it.first }) { f, which ->
                items[which].second(f)
            }
            .create()
            .show(fragmentManager, null)
    }

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
            R.string.prefs_settings_list_item_menu_copy_from_default to {
                AlertDialogFragment.Builder()
                    .setTitle(R.string.confirm_dialog_title)
                    .setMessage(R.string.prefs_settings_list_copy_shapes_description)
                    .setNegativeButton(R.string.dialog_cancel)
                    .setPositiveButton(R.string.dialog_ok) { f ->
                        viewModelScope.launch(Dispatchers.Main) {
                            runCatching {
                                application.preferencesRepository.copyShapesFromDefault(item.entity)
                                Toast.makeText(
                                    application.applicationContext,
                                    R.string.prefs_settings_list_copy_shapes_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.onFailure {
                                Toast.makeText(
                                    application.applicationContext,
                                    R.string.prefs_settings_list_copy_shapes_failure,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .create()
                    .show(fragmentManager, null)
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

        AlertDialogFragment.Builder()
            .setTitle(item.appName)
            .setNegativeButton(R.string.dialog_cancel)
            .setItems(items.map { it.first }) { f, which ->
                items[which].second(f)
            }
            .create()
            .show(fragmentManager, null)
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
            oldItem.entity.id == newItem.entity.id

        override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem) =
            oldItem.entity.lastUpdated == newItem.entity.lastUpdated
    }
}
