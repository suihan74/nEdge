package com.suihan74.nedge.scenes.preferences.dataBinding

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.viewpager2.widget.ViewPager2
import com.suihan74.nedge.scenes.preferences.MenuItem

object ViewPager2BindingAdapters {
    /**
     * 選択中メニュー項目をセットする
     */
    @JvmStatic
    @BindingAdapter("currentItem")
    fun setCurrentItem(viewPager: ViewPager2, menuItem: MenuItem?) {
        if (menuItem == null) return

        val nextIdx = MenuItem.values().indexOf(menuItem)
        if (viewPager.currentItem != nextIdx) {
            viewPager.currentItem = nextIdx
        }
    }

    /**
     * 現在表示中のページを選択中メニュー項目に反映する
     */
    @JvmStatic
    @InverseBindingAdapter(attribute = "currentItem")
    fun getCurrentItem(viewPager: ViewPager2) : MenuItem =
        MenuItem.values().getOrElse(viewPager.currentItem) { MenuItem.GENERAL }

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
