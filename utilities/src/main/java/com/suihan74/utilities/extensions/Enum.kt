package com.suihan74.utilities.extensions

import kotlin.reflect.KClass

/** Enum<T>::classから直接valuesを取得する */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Enum<T>> KClass<T>.getEnumConstants() =
    T::class.java.enumConstants as Array<out T>
