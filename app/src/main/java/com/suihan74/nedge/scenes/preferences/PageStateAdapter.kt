package com.suihan74.nedge.scenes.preferences

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * コンテンツページ遷移用アダプタ
 */
class PageStateAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = MenuItem.values().size

    override fun createFragment(position: Int): Fragment {
        val item = MenuItem.values()[position]
        return item.fragment()
    }
}
