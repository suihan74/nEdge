package com.suihan74.notificationreporter.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 画面情報を扱うリポジトリ */
class ScreenRepository {
    /** 画面が点灯している */
    val screenOn : LiveData<Boolean> by lazy { _screenOn }
    private val _screenOn = MutableLiveData<Boolean>()

    suspend fun setScreenState(isOn: Boolean) = withContext(Dispatchers.Main.immediate) {
        _screenOn.value = isOn
    }
}
