package com.suihan74.notificationreporter

import com.suihan74.notificationreporter.repositories.BatteryRepository

/**
 * アプリ情報
 */
class Application : android.app.Application() {
    companion object {
        lateinit var instance : Application
            private set
    }

    // ------ //

    /** バッテリー関係の情報を扱うリポジトリ */
    val batteryRepository by lazy {
        BatteryRepository().also {
            it.setBatterLevel(this)
        }
    }

    // ------ //

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
