package com.suihan74.notificationreporter.scenes.lockScreen.dataBinding

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.suihan74.notificationreporter.R

object ImageViewBindingAdapters {
    /** 充電レベル・充電状態に応じたバッテリアイコンを表示する */
    @JvmStatic
    @BindingAdapter("batteryLevel", "charging")
    fun setBatteryIcon(imageView: ImageView, batteryLevel: Int?, charging: Boolean?) {
        imageView.setImageDrawable(batteryLevel?.let {
            ContextCompat.getDrawable(
                imageView.context,
                if (charging == true) getChargingBatteryIconId(batteryLevel)
                else getStandardBatteryIconId(batteryLevel)
            )
        })
    }

    // ------ //

    /** 非充電時のバッテリアイコンを取得する */
    @DrawableRes
    fun getStandardBatteryIconId(level: Int) : Int = getResourceIdWithLevel(
        level,
        listOf(
            20 to R.drawable.ic_battery_20,
            30 to R.drawable.ic_battery_30,
            50 to R.drawable.ic_battery_50,
            60 to R.drawable.ic_battery_60,
            80 to R.drawable.ic_battery_80,
            90 to R.drawable.ic_battery_90,
            100 to R.drawable.ic_battery_100
        )
    )

    /** 充電中のバッテリアイコンを取得する */
    @DrawableRes
    fun getChargingBatteryIconId(level: Int) : Int = getResourceIdWithLevel(
        level,
        listOf(
            20 to R.drawable.ic_battery_charging_20,
            30 to R.drawable.ic_battery_charging_30,
            50 to R.drawable.ic_battery_charging_50,
            60 to R.drawable.ic_battery_charging_60,
            80 to R.drawable.ic_battery_charging_80,
            90 to R.drawable.ic_battery_charging_90,
            100 to R.drawable.ic_battery_charging_100
        )
    )

    /** 指定レベル以下のときに対応する画像リソースIDを取得する */
    @DrawableRes
    private fun getResourceIdWithLevel(level: Int, params: List<Pair<Int, Int>>) : Int =
        params.sortedBy { it.first }.first { level <= it.first }.second
}
