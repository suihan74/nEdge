package com.suihan74.notificationreporter.repositories

import androidx.lifecycle.MutableLiveData

/** 画面情報を扱うリポジトリ */
class ScreenRepository {
    /** 画面が点灯している */
    val screenOn = MutableLiveData<Boolean>()
}
