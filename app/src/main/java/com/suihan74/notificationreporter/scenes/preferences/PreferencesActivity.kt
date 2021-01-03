package com.suihan74.notificationreporter.scenes.preferences

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.ActivityPreferencesBinding
import com.suihan74.notificationreporter.databinding.ListHeaderPreferencesMenuBinding
import com.suihan74.notificationreporter.databinding.ListItemPreferencesMenuBinding
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.lazyProvideViewModel

/**
 * 設定画面
 */
class PreferencesActivity : AppCompatActivity() {

    val viewModel by lazyProvideViewModel {
        val app = Application.instance
        PreferencesViewModel(app.preferencesRepository)
    }

    private lateinit var binding: ActivityPreferencesBinding

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.PreferencesActivity)

        hideSystemUI()

        binding = DataBindingUtil.setContentView<ActivityPreferencesBinding>(
            this,
            R.layout.activity_preferences
        ).also {
            it.lifecycleOwner = this
        }

        // ページ選択メニュー
        initializeMenu(binding)

        // ページビュー
        binding.contentPager.also { pager ->
            pager.adapter = PageStateAdapter(supportFragmentManager, lifecycle)
            pager.fakeDragBy(0.2f)
        }
    }

    // スクリーン輪郭線・ノッチ輪郭線の描画がウィンドウアタッチ後でないとできないため
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.vm = viewModel

        // 画面明度のプレビュー
        viewModel.previewLightLevel.observe(this, {
            window.attributes = window.attributes.also { lp ->
                lp.screenBrightness =
                    when {
                        // システム設定値(-1.0fよりも小さい値のとき)
                        it == null || it < -1.0f -> -1.0f

                        // バックライト0+さらに暗くする
                        it < .0f -> 0.01f

                        // バックライト使用
                        else -> 0.01f + (1.0f - 0.01f) * it
                    }
            }
        })

        viewModel.editingLightLevel.observe(this, {
            when (it) {
                PreferencesViewModel.EditingLightLevel.NONE ->
                    observeScreenBrightness(null)

                PreferencesViewModel.EditingLightLevel.ON ->
                    observeScreenBrightness(viewModel.lightLevelOn)

                PreferencesViewModel.EditingLightLevel.OFF ->
                    observeScreenBrightness(viewModel.lightLevelOff)

                else -> {}
            }
        })
    }

    private var previewLightLevelObserver : Observer<Float>? = null

    private fun observeScreenBrightness(liveData: LiveData<Float>?) {
        previewLightLevelObserver?.let { observer ->
            viewModel.lightLevelOn.removeObserver(observer)
            viewModel.lightLevelOff.removeObserver(observer)
        }

        if (liveData == null) {
            viewModel.previewLightLevel.value = -100.0f
            return
        }

        val observer = Observer<Float> {
            viewModel.previewLightLevel.value = it
        }
        previewLightLevelObserver = observer

        liveData.observe(this, observer)
    }

    /** ダイアログなどから復帰時にImmersiveモードを再適用する */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    // ------ //

    /** ページ選択メニューの準備 */
    @Suppress("ClickableViewAccessibility")
    private fun initializeMenu(binding: ActivityPreferencesBinding) {
        val list = binding.menuRecyclerView.also { list ->
            val adapter = BindingListAdapter<MenuItem, ListItemPreferencesMenuBinding>(
                R.layout.list_item_preferences_menu,
                this,
                MenuItem.DiffCallback(),
            ) { binding, item ->
                binding.item = item
                binding.selectedItem = viewModel.selectedMenuItem
            }

            list.adapter = adapter.apply {
                setOnClickItemListener { binding ->
                    viewModel.selectedMenuItem.value = binding.item
                }

                submit(
                    items = MenuItem.values().toList(),
                    header = { parent ->
                        ListHeaderPreferencesMenuBinding.inflate(layoutInflater, parent, false).root.also {
                            it.setOnClickListener {}
                            it.setOnLongClickListener { false }
                        }
                    }
                )
            }
        }

        // `MotionLayout`にタッチイベントを伝播させる
        // リスト、各項目のタッチイベント処理で伝播が止まってしまうので、
        // その前に明示的に`MotionLayout`にもイベントを送り付けるようにしている

        list.setOnTouchListener { _, motionEvent ->
            binding.motionLayout.onTouchEvent(motionEvent)
            return@setOnTouchListener false
        }

        list.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                binding.motionLayout.onTouchEvent(e)
                return false
            }
        })
    }

    // ------ //

    fun hideSystemUI() {
        // 全画面表示する
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    fun showSystemUI() {
        window.decorView.let {
            it.systemUiVisibility =
                it.systemUiVisibility xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }
}
