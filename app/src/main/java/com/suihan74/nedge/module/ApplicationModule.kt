package com.suihan74.nedge.module

import android.content.Context
import androidx.datastore.dataStore
import androidx.room.Room
import com.suihan74.nedge.Application
import com.suihan74.nedge.dataStore.PreferencesSerializer
import com.suihan74.nedge.database.AppDatabase
import com.suihan74.nedge.database.Migration1to2
import com.suihan74.nedge.receivers.BatteryStateReceiver
import com.suihan74.nedge.receivers.ScreenReceiver
import com.suihan74.nedge.repositories.BatteryRepository
import com.suihan74.nedge.repositories.NotificationRepository
import com.suihan74.nedge.repositories.PreferencesRepository
import com.suihan74.nedge.repositories.ScreenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    /** 設定データストアの保存先ファイル名 */
    private const val PREFERENCES_DATA_STORE_NAME = "settings.ds"

    private val Context.dataStore by dataStore(
        fileName = PREFERENCES_DATA_STORE_NAME,
        serializer = PreferencesSerializer()
    )

    // ------ //

    @Provides
    fun provideApplication(@ApplicationContext context: Context) = context as Application

    // ------ //

    @Singleton
    @Provides
    @BatteryRepositoryQualifier
    fun provideBatteryRepository(@ApplicationContext context: Context) =
        BatteryRepository().also {
            GlobalScope.launch { it.setBatterLevel(context) }
        }

    @Singleton
    @Provides
    fun provideNotificationRepository() = NotificationRepository()

    @Singleton
    @Provides
    fun provideScreenRepository(@ApplicationContext context: Context) =
        ScreenRepository().also {
            GlobalScope.launch {
                it.setScreenState(context)
            }
        }

    @Singleton
    @Provides
    fun providePreferencesRepository(
        @ApplicationContext context: Context,
        @AppDatabaseQualifier database: AppDatabase
    ) = PreferencesRepository(
        dataStore = context.dataStore,
        notificationDao = database.notificationDao()
    )

    // ------ //

    @Singleton
    @Provides
    fun provideBatteryStateReceiver(@BatteryRepositoryQualifier batteryRepository: BatteryRepository) =
        BatteryStateReceiver(batteryRepository)

    @Singleton
    @Provides
    fun provideScreenReceiver() = ScreenReceiver()

    // ------ //

    @Singleton
    @Provides
    @AppDatabaseQualifier
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "app-db")
            .addMigrations(
                Migration1to2()
            )
            .build()
}

// ------ //

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BatteryRepositoryQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppDatabaseQualifier
