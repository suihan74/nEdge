package com.suihan74.notificationreporter.scenes.preferences.dataBinding

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.suihan74.notificationreporter.models.KeywordMatchingType
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

object TextViewBindingAdapters {
    /**
     * 時刻を表示する
     */
    @JvmStatic
    @BindingAdapter(value = ["localTime", "dateTimeFormat"], requireAll = false)
    fun setLocalTimeInt(textView: TextView, value: LocalTime?, dateTimeFormat: String?) {
        textView.text =
            if (value == null) ""
            else try {
                val formatter = DateTimeFormatter.ofPattern(dateTimeFormat ?: "HH:mm")
                value.format(formatter)
            }
            catch (e: DateTimeException) {
                Log.d("localTime", Log.getStackTraceString(e))
                ""
            }
    }

    /**
     * `Color`データを色コード文字列に変換して表示
     */
    @JvmStatic
    @BindingAdapter(value = ["colorCode", "prefix", "textColorLight", "textColorDark"], requireAll = false)
    fun setColorCodeText(
        textView: TextView,
        color: Int?,
        prefix: String?,
        textColorLight: Int?,
        textColorDark: Int?
    ) {
        if (color == null) {
            textView.text = ""
            return
        }

        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        val rHex = String.format("%02x", r)
        val gHex = String.format("%02x", g)
        val bHex = String.format("%02x", b)

        textView.text = buildString {
            if (prefix != null) {
                append(prefix)
            }
            append(rHex, gHex, bHex)
        }

        if (r > 127 && g > 127 && b > 127 && textColorDark != null) {
            textView.setTextColor(textColorDark)
        }
        else if (textColorLight != null) {
            textView.setTextColor(textColorLight)
        }
    }

    /**
     * アプリ名を表示
     */
    @JvmStatic
    @BindingAdapter(value = ["applicationName", "defaultName"], requireAll = false)
    fun setApplicationName(textView: TextView, appInfo: ApplicationInfo?, defaultName: String?) {
        textView.text =
            if (appInfo == null) defaultName.orEmpty()
            else textView.context.packageManager.getApplicationLabel(appInfo)
    }

    /**
     * 設定リスト項目のキーワード情報を表示
     */
    @JvmStatic
    @BindingAdapter("keywordMatchingType", "keyword")
    fun setKeywordData(textView: TextView, keywordMatchingType: KeywordMatchingType?, keyword: String?) {
        when {
            keywordMatchingType == null || keywordMatchingType == KeywordMatchingType.NONE || keyword.isNullOrBlank() -> {
                textView.text = ""
                textView.visibility = TextView.GONE
            }
            else -> {
                val context = textView.context
                textView.text = String.format("%s: %s",
                    context.getString(keywordMatchingType.textId),
                    keyword
                )
                textView.visibility = TextView.VISIBLE
            }
        }
    }
}
