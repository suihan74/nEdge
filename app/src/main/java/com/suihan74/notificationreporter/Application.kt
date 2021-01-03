package com.suihan74.notificationreporter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.datastore.createDataStore
import androidx.room.Room
import androidx.work.*
import com.jakewharton.threetenabp.AndroidThreeTen
import com.suihan74.notificationreporter.dataStore.PreferencesSerializer
import com.suihan74.notificationreporter.database.AppDatabase
import com.suihan74.notificationreporter.receivers.BatteryStateReceiver
import com.suihan74.notificationreporter.receivers.ScreenReceiver
import com.suihan74.notificationreporter.repositories.BatteryRepository
import com.suihan74.notificationreporter.repositories.NotificationRepository
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import com.suihan74.notificationreporter.repositories.ScreenRepository
import com.suihan74.utilities.VersionUtil
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/**
 * アプリ情報
 */
class Application : android.app.Application() {
    companion object {
        private var _instance : Application? = null
        val instance : Application
            get() = _instance!!

        /** 通知チャンネル: 通知テスト用 */
        private const val NOTIFICATION_CHANNEL_DUMMY = "DummyNotificationChannel"

        /** 設定データストアの保存先ファイル名 */
        private const val PREFERENCES_DATA_STORE_NAME = "settings.ds"
    }

    // ------ //

    /** バッテリー関係の情報を扱うリポジトリ */
    val batteryRepository by lazy {
        BatteryRepository().also {
            coroutineScope.launch {
                it.setBatterLevel(this@Application)
            }
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

    /** アプリレベルのコルーチンスコープ */
    private var _coroutineScope : CoroutineScope? = null
    val coroutineScope : CoroutineScope
        get() = _coroutineScope!!

    // ------ //

    override fun onCreate() {
        super.onCreate()
        _instance = this

        _coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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
                dataStore =
                    createDataStore(
                        fileName = PREFERENCES_DATA_STORE_NAME,
                        serializer = PreferencesSerializer()
                    ),
                notificationDao = db.notificationDao()
            )
        }

        // アプリが使用する通知チャンネルを作成する
        createNotificationChannel(NOTIFICATION_CHANNEL_DUMMY)

        Log.d("Application", "created")
    }

    override fun onTerminate() {
        super.onTerminate()
        _coroutineScope?.cancel()
        _coroutineScope = null
        _instance = null

        Log.d("Application", "terminated")
    }

    // ------ //

    fun notifyDummy(delay: Long, id: Int, message: String) {
        val params = Data.Builder()
            .putInt(DummyNotifyWorker.Arg.ID.name, id)
            .putString(DummyNotifyWorker.Arg.MESSAGE.name, message)
            .build()

        val request = OneTimeWorkRequestBuilder<DummyNotifyWorker>()
            .setInputData(params)
            .setInitialDelay(delay, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this)
            .enqueue(request)
    }

    /** ダミーの通知を発生させる */
    private fun notifyDummyImpl(id: Int, message: String) {
        createNotificationChannel(NOTIFICATION_CHANNEL_DUMMY)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_DUMMY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("dummy")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(id, builder.build())
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

    class DummyNotifyWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
        enum class Arg {
            ID,
            MESSAGE
        }

        private val id = params.inputData.getInt(Arg.ID.name, 0)

        private val message = params.inputData.getString(Arg.MESSAGE.name) ?: "dummy"

        override fun doWork(): Result {
            instance.notifyDummyImpl(id, message)
            return Result.success()
        }
    }
}

