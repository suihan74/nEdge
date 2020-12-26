package com.suihan74.utilities.dataBinding

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.viewpager2.widget.ViewPager2
import com.suihan74.notificationreporter.scenes.preferences.MenuItem

object ViewPager2BindingAdapters {
    /**
     * 選択中メニュー項目をセットする
     */
    @JvmStatic
    @BindingAdapter("currentItem")
    fun setCurrentItem(viewPager: ViewPager2, menuItem: MenuItem?) {
        if (menuItem == null) return
        viewPager.currentItem = MenuItem.values().indexOf(menuItem)
    }

    /**
     * 現在表示中のページを選択中メニュー項目に反映する
     */
    @JvmStatic
    @InverseBindingAdapter(attribute = "currentItem")
    fun getCurrentItem(viewPager: ViewPager2) : MenuItem =
        MenuItem.values()[viewPager.currentItem]

    /**
     * 双方向バインド用の設定
     */
    @JvmStatic
    @BindingAdapter("currentItemAttrChanged")
    fun bindListeners(viewPager: ViewPager2, listener: InverseBindingListener) {
        viewPager.setPageTransformer { _, _ ->
            listener.onChange()
        }
    }
}
