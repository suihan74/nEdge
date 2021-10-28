package com.suihan74.nedge.scenes.preferences.dialog

import android.app.Dialog
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import com.suihan74.nedge.R
import com.suihan74.nedge.databinding.DialogApplicationSelectionBinding
import com.suihan74.nedge.databinding.ListItemApplicationItemsBinding
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.DialogListener
import com.suihan74.utilities.extensions.hideSoftInputMethod
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApplicationSelectionDialogFragment : DialogFragment() {

    companion object {
        fun createInstance(
            onSelectListener: DialogListener<ApplicationItem>? = null
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
        val binding = DialogApplicationSelectionBinding.inflate(
            layoutInflater,
            null,
            false
        ).also {
            it.vm = viewModel
            it.lifecycleOwner = this
            initializeRecyclerView(it, this)
        }

        binding.searchEditText.also { editText ->
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    editText.hideSoftInputMethod(binding.recyclerView)
                    true
                }
                else false
            }
        }

        return AlertDialog.Builder(requireContext(), R.style.Theme_Dialog)
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
            viewModel.onSelect?.invoke(this, it.item!!)
            dismiss()
        }

        binding.recyclerView.adapter = adapter
    }

    // ------ //

    /** アプリを選択したときの処理をセット */
    fun setOnSelectListener(l: DialogListener<ApplicationItem>?) = lifecycleScope.launchWhenCreated {
        viewModel.onSelect = l
    }

    // ------ //

    class DialogViewModel(context: Context) : ViewModel() {
        /** インストール済みアプリ一覧 */
        val applications : LiveData<List<ApplicationItem>> by lazy { _applications }
        private val _applications = MutableLiveData<List<ApplicationItem>>()

        /** (検索クエリによるフィルタリングが施された表示用の)アプリ一覧 */
        val filteredApplications : LiveData<List<ApplicationItem>> by lazy { _filteredApplications }
        private val _filteredApplications = MutableLiveData<List<ApplicationItem>>()

        /** 検索クエリ */
        val searchQuery = MutableLiveData<String>().also {
            it.observeForever { query ->
                viewModelScope.launch {
                    createList(query, applications.value)
                }
            }
        }

        // ------ //

        var onSelect : DialogListener<ApplicationItem>? = null

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
                createList(searchQuery.value, apps)
            }
        }

        private suspend fun createList(query: String?, apps: List<ApplicationItem>?) = withContext(Dispatchers.Default) {
            if (query.isNullOrBlank()) {
                _filteredApplications.postValue(apps.orEmpty())
            }
            else {
                val q = query.lowercase()
                _filteredApplications.postValue(
                    apps.orEmpty().filter { item ->
                        item.appName.lowercase().contains(q)
                    }
                )
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
