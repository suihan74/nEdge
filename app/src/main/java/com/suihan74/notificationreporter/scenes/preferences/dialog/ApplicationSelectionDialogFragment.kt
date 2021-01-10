package com.suihan74.notificationreporter.scenes.preferences.dialog

import android.app.Dialog
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.DialogApplicationSelectionBinding
import com.suihan74.notificationreporter.databinding.ListItemApplicationItemsBinding
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.DialogListener
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApplicationSelectionDialogFragment : DialogFragment() {

    companion object {
        fun createInstance(
            onSelectListener: DialogListener<ApplicationInfo>? = null
        ) = ApplicationSelectionDialogFragment().also {
            it.setOnSelectListener(onSelectListener)
        }
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        DialogViewModel(requireContext())
    }

    // ------ //

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val lifecycleOwner = parentFragment?.viewLifecycleOwner ?: requireActivity()
        val binding = DialogApplicationSelectionBinding.inflate(
            LayoutInflater.from(requireContext()),
            null,
            false
        ).also {
            it.vm = viewModel
            it.lifecycleOwner = lifecycleOwner
            initializeRecyclerView(it, lifecycleOwner)
        }

        return AlertDialog.Builder(requireContext(), R.style.AlertDialog)
            .setTitle(R.string.pref_app_selection_dialog_title)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setView(binding.root)
            .create()
    }

    /** リストを初期化 */
    private fun initializeRecyclerView(binding: DialogApplicationSelectionBinding, lifecycleOwner: LifecycleOwner) {
        val adapter = BindingListAdapter<ApplicationItem, ListItemApplicationItemsBinding>(
            R.layout.list_item_application_items,
            lifecycleOwner,
            ApplicationItem.DiffCallback()
        ) { bind, item -> bind.item = item }

        adapter.setOnClickItemListener {
            viewModel.onSelect?.invoke(this, it.item!!.applicationInfo)
            dismiss()
        }

        binding.recyclerView.adapter = adapter
    }

    // ------ //

    /** アプリを選択したときの処理をセット */
    fun setOnSelectListener(l: DialogListener<ApplicationInfo>?) = lifecycleScope.launchWhenCreated {
        viewModel.onSelect = l
    }

    // ------ //

    class DialogViewModel(context: Context) : ViewModel() {
        /** インストール済みアプリ一覧 */
        val applications : LiveData<List<ApplicationItem>> by lazy { _applications }
        private val _applications = MutableLiveData<List<ApplicationItem>>()

        // ------ //

        var onSelect : DialogListener<ApplicationInfo>? = null

        // ------ //

        init {
            viewModelScope.launch(Dispatchers.Default) {
                val pm = context.packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val apps = packages
                    .map {
                        ApplicationItem(
                            pm.getApplicationLabel(it).toString(),
                            it
                        )
                    }
                    .sortedBy { it.appName }
                _applications.postValue(apps)
            }
        }
    }

    // ------ //

    /**
     * アプリとそれに設定された通知設定
     */
    data class ApplicationItem(
        /** アプリの表示名 */
        val appName : String,
        /** アプリ情報 */
        val applicationInfo : ApplicationInfo,
    ) {
        class DiffCallback : DiffUtil.ItemCallback<ApplicationItem>() {
            override fun areItemsTheSame(oldItem: ApplicationItem, newItem: ApplicationItem): Boolean {
                return oldItem.applicationInfo.packageName == newItem.applicationInfo.packageName
            }

            override fun areContentsTheSame(
                oldItem: ApplicationItem,
                newItem: ApplicationItem
            ): Boolean {
                return oldItem.applicationInfo.packageName == newItem.applicationInfo.packageName
            }
        }
    }
}