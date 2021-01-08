package com.suihan74.notificationreporter.scenes.preferences.dataBinding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.suihan74.notificationreporter.databinding.ListItemApplicationItemsBinding
import com.suihan74.notificationreporter.scenes.preferences.dialog.ApplicationSelectionDialogFragment.ApplicationItem
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.extensions.alsoAs

object RecyclerViewBindingAdapters {
    /** アプリ一覧を表示 */
    @JvmStatic
    @BindingAdapter("applications")
    fun setApplications(
        recyclerView: RecyclerView,
        items: List<ApplicationItem>?
    ) {
        recyclerView.adapter.alsoAs<BindingListAdapter<ApplicationItem, ListItemApplicationItemsBinding>> { adapter ->
            adapter.submit(items)
        }
    }
}
