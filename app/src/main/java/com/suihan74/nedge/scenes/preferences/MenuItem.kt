package com.suihan74.nedge.scenes.preferences

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.suihan74.nedge.R
import com.suihan74.nedge.scenes.preferences.page.*

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

    NOTIFICATION_SETTINGS(
        R.string.prefs_menu_label_notification_settings,
        R.drawable.ic_apps,
        { SettingsListFragment.createInstance() }
    ),

    BLACK_LIST(
        R.string.prefs_menu_label_black_list,
        R.drawable.ic_notifications_off,
        { BlackListFragment.createInstance() }
    ),

    INFORMATION(
        R.string.prefs_menu_label_information,
        R.drawable.ic_info,
        { InformationFragment.createInstance() }
    ),

    RELEASE_NOTES(
        R.string.prefs_menu_label_release_notes,
        R.drawable.ic_update,
        { ReleaseNotesFragment.createInstance() }
    ),

    ;

    class DiffCallback : DiffUtil.ItemCallback<MenuItem>() {
        override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem) =
            oldItem.name == newItem.name
    }
}
