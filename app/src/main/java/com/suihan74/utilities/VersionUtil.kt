@file:Suppress("unused")

package com.suihan74.utilities

/**
 * 規則通りに設定されたバージョンコードからMajor,Minor,Fix,Devバージョンを計算する
 *
 * バージョンコードの数値は
 * `Major(1桁)_Minor(2桁)_Fix(3桁)_Dev(3桁)`
 * の整数で設定する
 */
object VersionUtil {
    /** バージョンコード値からメジャーバージョンを計算する */
    fun getMajorVersion(versionCode: Long) : Long =
        versionCode / 100000000

    /** バージョンコード値からマイナーバージョンを計算する */
    fun getMinorVersion(versionCode: Long) : Long {
        val upperMask = 100000000
        val lowerMask = 1000000
        return (versionCode % upperMask) / lowerMask
    }

    /** バージョンコード値から修正バージョンを計算する */
    fun getFixVersion(versionCode: Long) : Long {
        val upperMask = 1000000
        val lowerMask = 1000
        return (versionCode % upperMask) / lowerMask
    }

    /** バージョンコード値から修正バージョンを計算する */
    fun getDevelopVersion(versionCode: Long) : Long =
        versionCode / 1000
}
