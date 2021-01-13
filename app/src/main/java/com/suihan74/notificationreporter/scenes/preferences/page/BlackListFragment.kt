package com.suihan74.notificationreporter.scenes.preferences.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.FragmentBlackListBinding
import com.suihan74.notificationreporter.databinding.ListItemBlackListItemsBinding
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.lazyProvideViewModel

/**
 * ブラックリスト設定画面
 */
class BlackListFragment : Fragment() {
    companion object {
        fun createInstance() = BlackListFragment()
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        BlackListViewModel(Application.instance)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBlackListBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        // リスト初期化
        initializeRecyclerView(binding.recyclerView)

        // 設定追加ボタン
        binding.addSettingButton.setOnClickListener {
            viewModel.createNewBlackListItem(childFragmentManager)
        }

        return binding.root
    }

    // ------ //

    /**
     * 通知表示設定リスト初期化
     */
    private fun initializeRecyclerView(recyclerView: RecyclerView) {
        val adapter = BindingListAdapter<BlackListItem, ListItemBlackListItemsBinding>(
            R.layout.list_item_black_list_items,
            viewLifecycleOwner,
            BlackListItem.DiffCallback()
        ) { binding, item -> binding.item = item }

        recyclerView.adapter = adapter

        adapter.setOnClickItemListener {
            viewModel.openBlackListItemEditorDialog(it.item!!, childFragmentManager)
        }

        adapter.setOnLongClickItemListener {
            viewModel.openBlackListItemMenuDialog(it.item!!, childFragmentManager)
        }

        viewModel.settings.observe(viewLifecycleOwner, {
            adapter.submit(items = it)
        })
    }
}
