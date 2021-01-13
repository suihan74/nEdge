package com.suihan74.notificationreporter.scenes.preferences.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.azeesoft.lib.colorpicker.ColorPickerDialog
import com.suihan74.notificationreporter.R
import com.suihan74.utilities.DialogListener
import com.suihan74.utilities.Listener
import com.suihan74.utilities.fragment.withArguments
import com.suihan74.utilities.lazyProvideViewModel

class ColorPickerDialogFragment : DialogFragment() {
    companion object {
        fun createInstance(initialColor: Int = Color.WHITE) = ColorPickerDialogFragment().withArguments {
            putInt(Arg.INITIAL_COLOR.name, initialColor)
        }

        enum class Arg {
            INITIAL_COLOR
        }
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        val args = requireArguments()
        DialogViewModel(args.getInt(Arg.INITIAL_COLOR.name))
    }

    // ------ //

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return ColorPickerDialog.createColorPickerDialog(requireContext(), R.style.ColorPickerDialog).also {
            it.setInitialColor(viewModel.initialColor)
            it.setPositiveActionText(getString(R.string.dialog_ok))
            it.setNegativeActionText(getString(R.string.dialog_cancel))
            it.setOnColorPickedListener { color, _ ->
                viewModel.onColorPicked?.invoke(this, color)
            }
        }
    }

    // ------ //

    fun setOnColorPickedListener(l : DialogListener<Int>?) = lifecycleScope.launchWhenCreated {
        viewModel.onColorPicked = l
    }

    fun setOnDismissListener(l : Listener<ColorPickerDialogFragment>?) = lifecycleScope.launchWhenCreated {
        viewModel.onDismiss = l
    }

    // ------ //

    class DialogViewModel(
        val initialColor: Int
    ) : ViewModel() {
        var onColorPicked : DialogListener<Int>? = null

        var onDismiss : Listener<ColorPickerDialogFragment>? = null
    }
}
