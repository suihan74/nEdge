package com.suihan74.notificationreporter.scenes.preferences.page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.databinding.FragmentGeneralPrefsBinding
import com.suihan74.notificationreporter.models.NotchSetting
import com.suihan74.notificationreporter.models.NotchType
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.notificationreporter.scenes.preferences.notch.RectangleNotchSettingFragment
import com.suihan74.notificationreporter.scenes.preferences.notch.WaterDropNotchSettingFragment
import com.suihan74.utilities.extensions.hideSoftInputMethod
import com.suihan74.utilities.fragment.AlertDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 全般設定画面
 */
class GeneralPrefsFragment : Fragment() {
    companion object {
        fun createInstance() = GeneralPrefsFragment()
    }

    // ------ //

    private val preferencesActivity
        get() = requireActivity() as PreferencesActivity

    private val viewModel
        get() = preferencesActivity.viewModel

    private var _binding : FragmentGeneralPrefsBinding? = null
    private val binding get() = _binding!!

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralPrefsBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        binding.previewButton.setOnClickListener {
            val intent = Intent(requireContext(), LockScreenActivity::class.java)
            startActivity(intent)
        }

        binding.notifyButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(5_000L)
                preferencesActivity.notifyDummy()
            }
        }

        binding.silentTimezoneStartButton.setOnClickListener {
            viewModel.openSilentTimezonePickerDialog(viewModel.silentTimezoneStart, childFragmentManager)
        }

        binding.silentTimezoneEndButton.setOnClickListener {
            viewModel.openSilentTimezonePickerDialog(viewModel.silentTimezoneEnd, childFragmentManager)
        }

        binding.colorEditText.also { editText ->
            // 入力中はナビゲーションバーを表示する
            editText.setOnFocusChangeListener { _, b ->
                if (b) {
                    preferencesActivity.window.decorView.let {
                        it.systemUiVisibility = it.systemUiVisibility xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    }
                }
                else {
                    preferencesActivity.hideSoftInputMethod(binding.mainLayout)
                    preferencesActivity.hideSystemUI()
                }
            }

            // 入力完了時にアンフォーカスする
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editText.clearFocus()
                }
                false
            }
        }

        binding.notchTypeSelectionButton.setOnClickListener {
            binding.colorEditText.clearFocus()
            val dialog = AlertDialogFragment.Builder()
                .setTitle(R.string.prefs_notch_type_selection_desc)
                .setItems(NotchType.values().map { it.name }) { _, which ->
                    viewModel.topNotchSetting.value = NotchSetting.createInstance(type = NotchType.values()[which])
                    viewModel.topNotchType.value = NotchType.values()[which]
                }
                .setNegativeButton(R.string.dialog_cancel)
                .create()
            dialog.show(childFragmentManager, null)
        }

        viewModel.topNotchType.observe(viewLifecycleOwner, {
            val fragment = when (it) {
                NotchType.RECTANGLE ->
                    RectangleNotchSettingFragment.createInstance(NotificationEntity.DEFAULT_SETTING_NAME)

                NotchType.WATER_DROP ->
                    WaterDropNotchSettingFragment.createInstance(NotificationEntity.DEFAULT_SETTING_NAME)

                else -> Fragment()
            }

            childFragmentManager.beginTransaction()
                .replace(R.id.notchSettingFragmentArea, fragment)
                .commit()
        })

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
