package com.suihan74.notificationreporter.scenes.preferences.page

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.database.notification.isDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        application.preferencesRepository.allNotificationSettingsFlow
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
        val appInfo = packageManager.getApplicationInfo(entity.appName, PackageManager.GET_META_DATA)
        return SettingItem(
            appName = packageManager.getApplicationLabel(appInfo).toString(),
            appInfo = appInfo,
            entity = entity
        )
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
                    oldItem.entity.keywordMatchingType == newItem.entity.keywordMatchingType
    }
}
