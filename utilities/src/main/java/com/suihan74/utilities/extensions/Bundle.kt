@file:Suppress("unused")

package com.suihan74.utilities.extensions

import android.os.Bundle

/** BundleにEnumをセットする */
inline fun <reified T : Enum<T>> Bundle.putEnum(key: String, value: T?) {
    if (value != null) {
        putInt(key, value.ordinal)
    }
    else if (containsKey(key)) {
        remove(key)
    }
}

/** BundleからEnumを取得する(失敗時null) */
inline fun <reified T : Enum<T>> Bundle.getEnum(key: String) : T? =
    try {
        (get(key) as? Int)?.let { ordinal ->
            T::class.getEnumConstants().getOrNull(ordinal)
        }
    }
    catch (e: Throwable) {
        e.printStackTrace()
        null
    }

/** BundleからEnumを取得する(失敗時デフォルト値) */
inline fun <reified T : Enum<T>> Bundle.getEnum(key: String, defaultValue: T) : T =
    getEnum<T>(key) ?: defaultValue

// --------- //

/** BundleにEnumをセットする(ordinal以外を使用) */
inline fun <reified T : Enum<T>> Bundle.putEnum(key: String, value: T?, selector: (T)->Int) {
    if (value != null) {
        putInt(key, selector(value))
    }
    else if (containsKey(key)) {
        remove(key)
    }
}

/** BundleからEnumを取得する(ordinal以外を使用, 失敗時null) */
inline fun <reified T : Enum<T>> Bundle.getEnum(key: String, selector: (T)->Int) : T? =
    try {
        (get(key) as? Int)?.let { intValue ->
            T::class.getEnumConstants().firstOrNull { selector(it) == intValue }
        }
    }
    catch (e: Throwable) {
        e.printStackTrace()
        null
    }

/** BundleからEnumを取得する(ordinal以外を使用, 失敗時デフォルト値) */
inline fun <reified T : Enum<T>> Bundle.getEnum(key: String, defaultValue: T, selector: (T)->Int) : T =
    getEnum(key, selector) ?: defaultValue

// ------ //

fun Bundle.getIntOrNull(key: String) : Int? = get(key) as? Int
