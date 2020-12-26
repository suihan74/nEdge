package com.suihan74.notificationreporter.scenes.preferences.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.FragmentInstalledApplicationsBinding
import com.suihan74.notificationreporter.databinding.ListItemApplicationItemsBinding
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.lazyProvideViewModel

/**
 * インストールされているアプリ一覧を表示するフラグメント
 */
class InstalledApplicationsFragment : Fragment() {
    companion object {
        fun createInstance() = InstalledApplicationsFragment()
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        val app = Application.instance
        InstalledApplicationsViewModel(app.preferencesRepository)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentInstalledApplicationsBinding.inflate(inflater, container, false).also {
            it.vm = viewModel.also { vm -> vm.init(requireContext()) }
            it.lifecycleOwner = viewLifecycleOwner
        }

        binding.recyclerView.also {
            it.setHasFixedSize(true)
            it.adapter = BindingListAdapter<ApplicationItem, ListItemApplicationItemsBinding>(
                R.layout.list_item_application_items,
                viewLifecycleOwner,
                ApplicationItem.DiffCallback()
            ) { b, item -> b.item = item }
        }

        return binding.root
    }
}
