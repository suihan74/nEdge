package com.suihan74.utilities.extensions

import org.threeten.bp.LocalTime

/**
 * 時刻が`start`~`end`の間であるかを調べる
 *
 * `start`>`end`の場合、渡された範囲が日を跨ぐものとして扱う
 * 
 * ex) `start`=23:00, `end`=07:00 の場合など
 */
fun LocalTime.between(start: LocalTime, end: LocalTime) : Boolean =
    when {
        start == end -> false

        start < end -> this in start..end

        else -> this >= start || this <= end
    }
