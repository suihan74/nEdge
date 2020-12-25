package com.suihan74.notificationreporter.scenes.preferences.dialog

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.suihan74.utilities.DialogListener
import com.suihan74.utilities.fragment.withArguments
import com.suihan74.utilities.lazyProvideViewModel
import org.threeten.bp.LocalTime

/**
 * 時刻選択ダイアログ
 */
class TimePickerDialogFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    companion object {
        fun createInstance(
            initialHour: Int,
            initialMinute: Int,
            is24HourView: Boolean
        ) = TimePickerDialogFragment().withArguments {
            putInt(ARG_INITIAL_HOUR, initialHour)
            putInt(ARG_INITIAL_MINUTE, initialMinute)
            putBoolean(ARG_IS_24_HOUR_VIEW, is24HourView)
        }

        private const val ARG_INITIAL_HOUR = "ARG_INITIAL_HOUR"
        private const val ARG_INITIAL_MINUTE = "ARG_INITIAL_MINUTE"
        private const val ARG_IS_24_HOUR_VIEW = "ARG_IS_24_HOUR_VIEW"
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        DialogViewModel()
    }

    // ------ //

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val (hour, minute, is24HourView) = requireArguments().let { args ->
            Triple(
                args.getInt(ARG_INITIAL_HOUR),
                args.getInt(ARG_INITIAL_MINUTE),
                args.getBoolean(ARG_IS_24_HOUR_VIEW)
            )
        }

        return TimePickerDialog(context, this, hour, minute, is24HourView)
    }

    override fun onTimeSet(timePicker: TimePicker?, hour: Int, minute: Int) {
        viewModel.onTimeSetListener?.invoke(this, LocalTime.of(hour, minute))
    }

    // ------ //

    /**
     * 選択完了時の処理をセット
     */
    fun setOnTimeSetListener(l: DialogListener<LocalTime>?) = lifecycleScope.launchWhenCreated {
        viewModel.onTimeSetListener = l
    }

    // ------ //

    class DialogViewModel : ViewModel() {
        var onTimeSetListener : DialogListener<LocalTime>? = null
    }
}
