package com.suihan74.nedge.scenes.preferences.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.databinding.FragmentReleaseNotesBinding
import com.suihan74.nedge.databinding.ListHeaderReleaseNotesBinding
import com.suihan74.nedge.databinding.ListItemReleaseNotesBinding
import com.suihan74.nedge.models.ReleaseNote
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.lazyProvideViewModel

/**
 * 更新履歴
 */
class ReleaseNotesFragment : Fragment() {
    companion object {
        fun createInstance() = ReleaseNotesFragment()
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        ReleaseNotesViewModel(Application.instance)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentReleaseNotesBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        initializeRecyclerView(binding.recyclerView)

        return binding.root
    }

    /**
     * 更新履歴リスト初期化
     */
    private fun initializeRecyclerView(recyclerView: RecyclerView) {
        val adapter = BindingListAdapter<ReleaseNote, ListItemReleaseNotesBinding>(
            R.layout.list_item_release_notes,
            viewLifecycleOwner,
            ReleaseNote.DiffCallback()
        ) { binding, item -> binding.item = item }

        adapter.setOnClickItemListener {
            // do nothing
        }

        recyclerView.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner, Observer {
            adapter.submit(
                items = it,
                header = { parent ->
                    val binding = ListHeaderReleaseNotesBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                    binding.root
                }
            )
        })
    }
}
