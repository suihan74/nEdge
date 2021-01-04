package com.suihan74.notificationreporter.repositories

import android.app.Service
import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 画面情報を扱うリポジトリ */
class ScreenRepository {
    /** 画面が点灯している */
    val screenOn : LiveData<Boolean> by lazy { _screenOn }
    private val _screenOn = MutableLiveData<Boolean>()

    /**
     * 画面点灯状態を変更する
     */
    suspend fun setScreenState(isOn: Boolean) = withContext(Dispatchers.Main.immediate) {
        _screenOn.value = isOn
    }

    /**
     * 現在の画面点灯状態を取得し記録する
     */
    suspend fun setScreenState(context: Context) {
        val pm = context.getSystemService(Service.POWER_SERVICE) as PowerManager
        setScreenState(pm.isInteractive)
    }
}
