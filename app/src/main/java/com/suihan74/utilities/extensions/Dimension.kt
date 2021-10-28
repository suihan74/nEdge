package com.suihan74.utilities.extensions

import com.suihan74.nedge.Application

private val density : Float
    get() =
        Application.instance.resources.displayMetrics.density

/**
 * dpで指定した数値をpxに変換する
 */
val Int.dp : Float
    get() = this * density

/**
 * dpで指定した数値をpxに変換する
 */
val Float.dp : Float
    get() = this * density
