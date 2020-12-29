package com.suihan74.notificationreporter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.room.Room
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jakewharton.threetenabp.AndroidThreeTen
import com.suihan74.notificationreporter.dataStore.PreferencesKey
import com.suihan74.notificationreporter.database.AppDatabase
import com.suihan74.notificationreporter.receivers.BatteryStateReceiver
import com.suihan74.notificationreporter.receivers.ScreenReceiver
import com.suihan74.notificationreporter.repositories.BatteryRepository
import com.suihan74.notificationreporter.repositories.NotificationRepository
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import com.suihan74.notificationreporter.repositories.ScreenRepository
import com.suihan74.utilities.VersionUtil
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * アプリ情報
 */
class Application : android.app.Application() {
    companion object {
        lateinit var instance : Application
            private set

        /** 通知チャンネル: 通知テスト用 */
        private const val NOTIFICATION_CHANNEL_DUMMY = "DummyNotificationChannel"
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

    /** 画面状態を扱うリポジトリ */
    val screenRepository by lazy {
        ScreenRepository()
    }

    /** アプリ設定を扱うリポジトリ */
    lateinit var preferencesRepository : PreferencesRepository

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

    /** データベースインスタンス */
    private val db by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java, "app-db"
        ).build()
    }

    // ------ //

    /** アプリのバージョン番号 */
    val versionCode: Long by lazy {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        PackageInfoCompat.getLongVersionCode(packageInfo)
    }

    /** アプリのバージョン名 */
    val versionName: String by lazy {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName
    }

    /** アプリのメジャーバージョン */
    val majorVersionCode: Long by lazy { VersionUtil.getMajorVersion(versionCode) }

    /** アプリのマイナーバージョン */
    val minorVersionCode: Long by lazy { VersionUtil.getMinorVersion(versionCode) }

    /** アプリの修正バージョン */
    val fixVersionCode: Long by lazy { VersionUtil.getFixVersion(versionCode) }

    /** アプリの開発バージョン */
    val developVersionCode: Long by lazy { VersionUtil.getDevelopVersion(versionCode) }

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

        // 設定リポジトリの用意
        runBlocking {
            preferencesRepository = PreferencesRepository(
                dataStore = PreferencesKey.dataStore(instance),
                notificationDao = db.notificationDao()
            )
        }

        // アプリが使用する通知チャンネルを作成する
        createNotificationChannel(NOTIFICATION_CHANNEL_DUMMY)
    }

    // ------ //

    fun notifyDummy(delay: Long) {
        val request = OneTimeWorkRequestBuilder<DummyNotifyWorker>()
            .setInitialDelay(delay, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this)
            .enqueue(request)
    }

    /** ダミーの通知を発生させる */
    private fun notifyDummyImpl() {
        val notificationId = 334

        createNotificationChannel(NOTIFICATION_CHANNEL_DUMMY)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_DUMMY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("dummy")
            .setContentText("dummy")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    /** 通知チャンネルを作成する */
    @Suppress("SameParameterValue")
    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val name = "dummy"
        val description = "dummy notifications for test"
        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT).also {
            it.description = description
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    class DummyNotifyWorker(
        appContext: Context,
        params: WorkerParameters
    ) : Worker(appContext, params) {
        override fun doWork(): Result {
            Application.instance.notifyDummyImpl()
            return Result.success()
        }
    }
}

