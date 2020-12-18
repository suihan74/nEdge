package com.suihan74.notificationreporter.models

/**
 * ノッチの種類
 */
enum class NotchType {
    /** ノッチ無し */
    NONE,

    /** 矩形 */
    RECTANGLE,

    /** 水滴 (O型も含む) */
    WATER_DROP,

    /** ○の切り抜き (辺に接しない) */
    PUNCH_HOLE,

    ;

    companion object {
        /**
         * 名前からインスタンスを取得
         *
         * @throws NoSuchElementException IDが存在しない
         */
        fun fromName(name: String) = values().first { it.name == name }
    }
}
