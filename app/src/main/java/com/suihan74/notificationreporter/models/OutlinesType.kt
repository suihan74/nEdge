package com.suihan74.notificationreporter.models

/**
 * 通知表示の外周縁取り方法
 */
enum class OutlinesType {
    /** 描画なし */
    NONE,

    /** 全周を囲う */
    FULL,

    /** 上辺のみ */
    TOP,

    /** 下辺のみ */
    BOTTOM,

    /** 上下辺 */
    HORIZONTAL,

    /** 左辺のみ */
    LEFT,

    /** 右辺のみ */
    RIGHT,

    /** 左右辺 */
    VERTICAL

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
