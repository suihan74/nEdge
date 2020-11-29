package com.suihan74.notificationreporter

import android.content.Intent
import android.content.IntentFilter
import com.jakewharton.threetenabp.AndroidThreeTen
import com.suihan74.notificationreporter.receivers.BatteryStateReceiver
import com.suihan74.notificationreporter.receivers.ScreenReceiver
import com.suihan74.notificationreporter.repositories.BatteryRepository
import com.suihan74.notificationreporter.repositories.NotificationRepository

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

    /** 通知を扱うリポジトリ */
    val notificationRepository by lazy {
        NotificationRepository()
    }

    // ------ //

    /** 画面消灯を監視するレシーバ */
    private val screenReceiver by lazy {
        ScreenReceiver()
    }

    /** バッテリ状態を監視するレシーバ */
    private val batteryStateReceiver by lazy {
        BatteryStateReceiver()
    }

    // ------ //

    override fun onCreate() {
        super.onCreate()
        instance = this

        // initialize the timezone information
        AndroidThreeTen.init(this)

        // 画面消灯を監視する
        registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))

        // バッテリ残量・充電状態変更を監視する
        registerReceiver(batteryStateReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        registerReceiver(batteryStateReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))
        registerReceiver(batteryStateReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
    }
}
