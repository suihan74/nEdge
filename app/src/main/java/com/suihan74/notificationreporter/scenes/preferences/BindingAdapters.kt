package com.suihan74.notificationreporter.scenes.preferences

import android.app.Activity
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.scenes.lockScreen.NotificationDrawer

object BindingAdapters {
    /**
     * `ImageView`に通知表示を描画する
     */
    @JvmStatic
    @BindingAdapter("notificationSetting")
    fun setNotificationSetting(imageView: ImageView, notificationSetting: NotificationSetting?) {
        if (notificationSetting == null) {
            imageView.setImageDrawable(null)
            return
        }

        try {
            val activity = imageView.context as? Activity ?: return
            val notificationDrawer = NotificationDrawer(activity.window)
            notificationDrawer.draw(imageView, notificationSetting)
        }
        catch (e: Throwable) {
            Log.e("notificationSetting", Log.getStackTraceString(e))
            imageView.setImageDrawable(null)
        }
    }

    /**
     * `Color`データを色コード文字列に変換して表示
     */
    @JvmStatic
    @BindingAdapter("colorCode")
    fun setColorCodeText(editText: EditText, color: Int?) {
        if (color == null) {
            editText.text.clear()
            return
        }

        val r = String.format("%02x", Color.red(color))
        val g = String.format("%02x", Color.green(color))
        val b = String.format("%02x", Color.blue(color))

        val code = buildString {
            append(r, g, b)
        }

        if (code != editText.text.toString()) {
            editText.text.clear()
            editText.text.append(code)
        }
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "colorCode")
    fun setColorCodeTextInverse(editText: EditText) : Int {
        val code = editText.text.toString()
        if (code.length != 6) return Color.WHITE

        val r = code.substring(0, 2).toInt(16)
        val g = code.substring(2, 4).toInt(16)
        val b = code.substring(4).toInt(16)

        return (0xff shl 24) or (r shl 16) or (g shl 8) or b
    }

    /** 双方向バインドのためのリスナを設定する */
    @JvmStatic
    @BindingAdapter("colorCodeAttrChanged")
    fun bindListeners(editText: EditText, colorCodeAttrChanged: InverseBindingListener?) {
        editText.doAfterTextChanged {
            if (it?.length == 6) {
                colorCodeAttrChanged?.onChange()
            }
        }
    }
}
