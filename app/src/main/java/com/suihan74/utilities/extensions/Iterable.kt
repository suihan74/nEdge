@file:Suppress("unused")

package com.suihan74.utilities.extensions

/**
 * 指定した型に変換できる最初の要素を探す
 *
 * @throws NoSuchElementException 該当する要素が見つからなかった
 */
inline fun <reified T> Iterable<*>.firstByType() : T {
    return this.first { it is T } as T
}

/**
 * 指定した型に変換できる最初の要素を探す
 *
 * @return 発見した最初の要素またはnull
 */
inline fun <reified T> Iterable<*>.firstOrNullByType() : T? {
    return (this.first { it is T } as? T)
}

/**
 * 指定した型に変換できる最初の要素を探す
 *
 * @throws NoSuchElementException 該当する要素が見つからなかった
 */
inline fun <reified T> Array<*>.firstByType() : T {
    return this.first { it is T } as T
}

/**
 * 指定した型に変換できる最初の要素を探す
 *
 * @return 発見した最初の要素またはnull
 */
inline fun <reified T> Array<*>.firstOrNullByType() : T? {
    return (this.first { it is T } as? T)
}
