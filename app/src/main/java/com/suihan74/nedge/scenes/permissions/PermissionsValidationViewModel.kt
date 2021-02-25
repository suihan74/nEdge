package com.suihan74.nedge.scenes.permissions

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.suihan74.nedge.receivers.DeviceAdminReceiver
import com.suihan74.nedge.scenes.preferences.PreferencesActivity

class PermissionsValidationViewModel : ViewModel() {
    /** ユーザーにパーミッションを手動で許可させる処理の合否を受け取るためのリクエストコード */
    enum class RequestCode {
        /** 画面の最前面に表示する */
        OVERLAY_PERMISSION,

        /** 他のアプリが発した通知を取得する */
        NOTIFICATION_LISTENER,

        /** 端末管理アプリとして登録されている */
        DEVICE_POLICY_MANAGER,
    }

    // ------ //

    companion object {
        /**
         * アプリの実行に必要なすべてのパーミッションが許可されている
         */
        fun allPermissionsAllowed(context: Context) : Boolean =
            manageOverlayPermitted(context) &&
                    notificationListenerPermitted(context) &&
                    devicePolicyManagerPermitted(context)

        /**
         * 画面最前面(ロック画面よりも上)に表示するためのパーミッション
         */
        fun manageOverlayPermitted(context: Context) : Boolean {
            return Settings.canDrawOverlays(context)
        }

        /**
         * 他のアプリが発した通知を取得するためのパーミッション
         */
        fun notificationListenerPermitted(context: Context) : Boolean {
            return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        }

        /**
         * 端末管理アプリに登録されている必要がある
         *
         * 画面を明示的にロックするため
         */
        fun devicePolicyManagerPermitted(context: Context) : Boolean {
            val dpm = context.getSystemService(Service.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = ComponentName(context, DeviceAdminReceiver::class.java)
            return dpm.isAdminActive(componentName)
        }
    }

    // ------ //

    val manageOverlayState : LiveData<Boolean> by lazy { _manageOverlayState }
    private val _manageOverlayState = MutableLiveData<Boolean>()

    val notificationListenerState : LiveData<Boolean> by lazy { _notificationListenerState }
    private val _notificationListenerState = MutableLiveData<Boolean>()

    val devicePolicyManagerState: LiveData<Boolean> by lazy { _devicePolicyManagerState }
    private val _devicePolicyManagerState = MutableLiveData<Boolean>()

    // ------ //

    /**
     * パーミッション許可状態を再取得する
     */
    fun refreshStates(context: Context) {
        _manageOverlayState.value = manageOverlayPermitted(context)
        _notificationListenerState.value = notificationListenerPermitted(context)
        _devicePolicyManagerState.value = devicePolicyManagerPermitted(context)
    }

    // ------ //

    private lateinit var requestPermissionLauncher : ActivityResultLauncher<Intent>

    fun onCreateActivity(activity: AppCompatActivity) {
        requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (allPermissionsAllowed(activity)) {
                launchContentsActivity(activity)
            }
        }
    }

    // ------ //

    /** アプリコンテンツ本体に遷移する */
    fun launchContentsActivity(activity: AppCompatActivity) {
        val intent = Intent(activity, PreferencesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
    }

    /**
     * 画面最前面(ロック画面よりも上)に表示するためのパーミッション要求
     */
    fun requestManageOverlayPermission(activity: AppCompatActivity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        requestPermissionLauncher.launch(intent)
    }

    /**
     * 他のアプリが発した通知を取得するためのパーミッション要求
     */
    fun requestNotificationListenerPermission(activity: AppCompatActivity) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        requestPermissionLauncher.launch(intent)
    }

    /**
     * 端末管理アプリに登録されている必要がある
     *
     * 画面を明示的にロックするため
     */
    fun requestDevicePolicyManagerPermission(activity: AppCompatActivity) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).also {
            val componentName = ComponentName(activity, DeviceAdminReceiver::class.java)
            it.putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                componentName
            )
        }
        requestPermissionLauncher.launch(intent)
    }
}
