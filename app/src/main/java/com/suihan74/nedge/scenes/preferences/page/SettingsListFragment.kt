package com.suihan74.nedge.scenes.preferences.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.databinding.FragmentSettingsListBinding
import com.suihan74.nedge.databinding.ListFooterWithFabBinding
import com.suihan74.nedge.databinding.ListHeaderNotificationSettingItemsBinding
import com.suihan74.nedge.databinding.ListItemNotificationSettingItemsBinding
import com.suihan74.nedge.scenes.preferences.PreferencesActivity
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.lazyProvideViewModel

/**
 * 通知表示設定リスト画面
 */
class SettingsListFragment : Fragment() {
    companion object {
        fun createInstance() = SettingsListFragment()
    }

    // ------ //

    private val preferencesActivity
        get() = requireActivity() as PreferencesActivity

    // ------ //

    private val viewModel by lazyProvideViewModel {
        SettingsListViewModel(Application.instance)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsListBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        // リスト初期化
        initializeRecyclerView(binding.recyclerView)

        // 設定追加ボタン
        binding.addSettingButton.setOnClickListener {
            viewModel.createNewNotificationSetting(childFragmentManager)
        }

        return binding.root
    }

    // ------ //

    /**
     * 通知表示設定リスト初期化
     */
    private fun initializeRecyclerView(recyclerView: RecyclerView) {
        val adapter = BindingListAdapter<SettingItem, ListItemNotificationSettingItemsBinding>(
            R.layout.list_item_notification_setting_items,
            viewLifecycleOwner,
            SettingItem.DiffCallback()
        ) { binding, item -> binding.item = item }

        recyclerView.adapter = adapter

        adapter.setOnClickItemListener {
            val entity = it.item!!.entity
            preferencesActivity.openSettingEditor(entity)
        }

        adapter.setOnLongClickItemListener {
            viewModel.openSettingItemMenuDialog(it.item!!, childFragmentManager)
        }

        viewModel.settings.observe(viewLifecycleOwner) {
            adapter.submit(
                items = it,
                header = { parent ->
                    val binding = ListHeaderNotificationSettingItemsBinding.inflate(layoutInflater, parent, false)
                    binding.mainLayout.setOnClickListener {
                        viewModel.defaultSettingEntity?.let { entity ->
                            preferencesActivity.openSettingEditor(entity)
                        }
                    }
                    binding.mainLayout.setOnLongClickListener {
                        viewModel.openDefaultSettingMenuDialog(childFragmentManager)
                        true
                    }
                    binding.root
                },
                footer = { parent ->
                    val binding = ListFooterWithFabBinding.inflate(layoutInflater, parent, false)
                    binding.root
                }
            )
        }
    }
}
