package com.suihan74.utilities.extensions

import android.app.Service
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import com.suihan74.nedge.Application

private val displayMetrics : DisplayMetrics?
    get() =
        Application.instance.getSystemService(Service.DISPLAY_SERVICE).letAs<DisplayManager, DisplayMetrics?> {
            it.displays.firstOrNull()?.let { display ->
                val metrics = DisplayMetrics()
                display.getMetrics(metrics)
                metrics
            }
        }

/**
 * dpで指定した数値をpxに変換する
 */
val Int.dp : Float
    get() = this * (displayMetrics?.density ?: 1.0f)

/**
 * dpで指定した数値をpxに変換する
 */
val Float.dp : Float
    get() = this * (displayMetrics?.density ?: 1.0f)
