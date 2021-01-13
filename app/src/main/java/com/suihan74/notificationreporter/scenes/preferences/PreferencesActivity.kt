package com.suihan74.notificationreporter.scenes.preferences

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.databinding.ActivityPreferencesBinding
import com.suihan74.notificationreporter.databinding.ListHeaderPreferencesMenuBinding
import com.suihan74.notificationreporter.databinding.ListItemPreferencesMenuBinding
import com.suihan74.notificationreporter.scenes.preferences.page.SettingEditorFragment
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.lazyProvideViewModel

/**
 * 設定画面
 */
class PreferencesActivity : AppCompatActivity() {

    val viewModel by lazyProvideViewModel {
        PreferencesViewModel(Application.instance)
    }

    private lateinit var binding: ActivityPreferencesBinding

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.PreferencesActivity)

        binding = ActivityPreferencesBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
        }
        setContentView(binding.root)

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
        viewModel.onAttachedToWindow(this, window)
        binding.vm = viewModel
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

    /** 通知設定編集画面を開く */
    fun openSettingEditor(entity: NotificationEntity) {
        val fragment = SettingEditorFragment.createInstance(entity)
        supportFragmentManager.beginTransaction()
            .replace(R.id.settingEditorFrame, fragment)
            .addToBackStack("settingEditorFrame")
            .commit()
    }

    /** 通知設定編集画面を閉じる */
    fun closeSettingEditor() {
        supportFragmentManager.popBackStackImmediate()
    }
}
