package com.suihan74.notificationreporter.scenes.preferences.page

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
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
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val items = packages
            .map {
                ApplicationItem(
                    pm.getApplicationLabel(it).toString(),
                    it
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
) {
    class DiffCallback : DiffUtil.ItemCallback<ApplicationItem>() {
        override fun areItemsTheSame(oldItem: ApplicationItem, newItem: ApplicationItem): Boolean {
            return oldItem.applicationInfo.packageName == newItem.applicationInfo.packageName
        }

        override fun areContentsTheSame(
            oldItem: ApplicationItem,
            newItem: ApplicationItem
        ): Boolean {
            return oldItem.applicationInfo.packageName == newItem.applicationInfo.packageName
        }
    }
}
