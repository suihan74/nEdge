package com.suihan74.utilities.extensions

import android.app.Service
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import com.suihan74.notificationreporter.Application

private val displayMetrics : DisplayMetrics?
    get() =
        Application.instance.getSystemService(Service.DISPLAY_SERVICE).letAs<DisplayManager, DisplayMetrics?> {
            it.displays.firstOrNull()?.let { display ->
                val metrics = DisplayMetrics()
                display.getMetrics(metrics)
                metrics
            }
        }

val Int.dp : Float
    get() = this * (displayMetrics?.density ?: 1.0f)
