package com.suihan74.notificationreporter.scenes.preferences.page

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InstalledApplicationsViewModel(
    private val prefsRepo : PreferencesRepository
) : ViewModel() {

    /** インストール済みのアプリ一覧 */
    val applications = MutableLiveData<List<ApplicationItem>>()

    // ------ //

    fun init(context: Context) = viewModelScope.launch(Dispatchers.Default) {
        /*
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
        resolveInfoList.forEach {
            Log.d("pm", "packageName: " + it.activityInfo.applicationInfo.name)
            Log.d("pm", "---")

        }
        */
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val items = packages
            .map {
                ApplicationItem(
                    pm.getApplicationLabel(it).toString(),
                    it,
                    prefsRepo.getNotificationSettingOrNull(it.packageName)
                )
            }
            .sortedBy { it.appName }
        applications.postValue(items)
    }
}

// ------ //

/**
 * アプリとそれに設定された通知設定
 */
data class ApplicationItem(
    /** アプリの表示名 */
    val appName : String,
    /** アプリ情報 */
    val applicationInfo : ApplicationInfo,
    /** アプリに対応する通知表示設定 */
    val notificationSetting : NotificationSetting?
) {
    class DiffCallback : DiffUtil.ItemCallback<ApplicationItem>() {
        override fun areItemsTheSame(oldItem: ApplicationItem, newItem: ApplicationItem): Boolean {
            return oldItem.applicationInfo.packageName == newItem.applicationInfo.packageName
        }

        override fun areContentsTheSame(
            oldItem: ApplicationItem,
            newItem: ApplicationItem
        ): Boolean {
            return oldItem.notificationSetting == newItem.notificationSetting
        }
    }
}
