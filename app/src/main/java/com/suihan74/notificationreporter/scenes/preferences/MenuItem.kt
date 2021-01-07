package com.suihan74.notificationreporter.scenes.preferences

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.scenes.preferences.page.GeneralPrefsFragment
import com.suihan74.notificationreporter.scenes.preferences.page.InformationFragment
import com.suihan74.notificationreporter.scenes.preferences.page.InstalledApplicationsFragment
import com.suihan74.notificationreporter.scenes.preferences.page.SettingsListFragment

/**
 * 設定画面ページ遷移用メニュー項目
 */
enum class MenuItem(
    @StringRes val labelId : Int,
    @DrawableRes val iconId : Int,
    val fragment : ()->Fragment
) {
    GENERAL(
        R.string.prefs_menu_label_generals,
        R.drawable.ic_settings,
        { GeneralPrefsFragment.createInstance() }
    ),

    APPLICATIONS(
        R.string.prefs_menu_label_applications,
        R.drawable.ic_apps,
        { InstalledApplicationsFragment.createInstance() }
    ),

    WHITE_LIST(
        R.string.prefs_menu_label_while_list,
        R.drawable.ic_notifications_active,
        { SettingsListFragment.createInstance() }
    ),

    BLACK_LIST(
        R.string.prefs_menu_label_black_list,
        R.drawable.ic_notifications_off,
        { Fragment() }
    ),

    INFORMATION(
        R.string.prefs_menu_label_information,
        R.drawable.ic_info,
        { InformationFragment.createInstance() }
    ),

    ;

    class DiffCallback : DiffUtil.ItemCallback<MenuItem>() {
        override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem) =
            oldItem.name == newItem.name
    }
}
