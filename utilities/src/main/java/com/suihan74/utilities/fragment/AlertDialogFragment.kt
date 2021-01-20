@file:Suppress("unused")

package com.suihan74.utilities.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.suihan74.utilities.Listener
import com.suihan74.utilities.extensions.getEnum
import com.suihan74.utilities.extensions.getIntOrNull
import com.suihan74.utilities.extensions.onNot
import com.suihan74.utilities.extensions.putEnum
import com.suihan74.utilities.lazyProvideViewModel

/**
 * 汎用的なダイアログフラグメント
 *
 * `AlertDialog`を`DialogFragment`で包んで作成し，扱うデータを`lifecycle`内で持続させる
 */
class AlertDialogFragment : DialogFragment() {
    companion object {
        private fun createInstance() = AlertDialogFragment().withArguments()

        private enum class Arg {
            THEME_ID,
            TITLE_ID,
            TITLE,
            MESSAGE_ID,
            MESSAGE,
            POSITIVE_BUTTON_TEXT_ID,
            NEGATIVE_BUTTON_TEXT_ID,
            NEUTRAL_BUTTON_TEXT_ID,
            ITEM_LABEL_IDS,
            ITEM_LABELS,
            ITEMS_MODE,
            SINGLE_CHECKED_ITEM,
            MULTI_CHECKED_ITEMS
        }
    }

    private val viewModel: DialogViewModel by lazyProvideViewModel {
        DialogViewModel(requireArguments())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return viewModel.createDialog(this)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onDismiss?.invoke(this)
    }

    // ------ //

    /** singleChoiceItemsで選択されている項目 */
    val checkedItem : Int
        get() = viewModel.checkedItem

    /** multiChoiceItemsでの各項目の選択状態 */
    val checkedItems : BooleanArray
        get() = viewModel.checkedItems

    // ------ //

    fun setDismissOnClickButton(flag: Boolean) = lifecycleScope.launchWhenCreated {
        viewModel.dismissOnClickButton = flag
    }

    fun setDismissOnClickItem(flag: Boolean) = lifecycleScope.launchWhenCreated {
        viewModel.dismissOnClickItem = flag
    }

    fun setOnClickPositiveButton(listener: Listener<AlertDialogFragment>?) = lifecycleScope.launchWhenCreated {
        viewModel.onClickPositiveButton = listener
    }

    fun setOnClickNegativeButton(listener: Listener<AlertDialogFragment>?) = lifecycleScope.launchWhenCreated {
        viewModel.onClickNegativeButton = listener
    }

    fun setOnClickNeutralButton(listener: Listener<AlertDialogFragment>?) = lifecycleScope.launchWhenCreated {
        viewModel.onClickNeutralButton = listener
    }

    fun setOnClickItem(listener: ((AlertDialogFragment, Int)->Unit)?) = lifecycleScope.launchWhenCreated {
        viewModel.onClickItem = listener
    }

    fun setOnDismissListener(listener: Listener<AlertDialogFragment>?) = lifecycleScope.launchWhenCreated {
        viewModel.onDismiss = listener
    }

    // ------ //

    class DialogViewModel(args: Bundle) : ViewModel() {
        @StyleRes
        val themeId: Int? = args.getIntOrNull(Arg.THEME_ID.name)

        @StringRes
        val titleId : Int = args.getInt(Arg.TITLE_ID.name, 0)

        val title : CharSequence? = args.getCharSequence(Arg.TITLE.name)

        @StringRes
        val messageId : Int = args.getInt(Arg.MESSAGE_ID.name, 0)

        val message : CharSequence? = args.getCharSequence(Arg.MESSAGE.name)

        @StringRes
        val positiveTextId : Int = args.getInt(Arg.POSITIVE_BUTTON_TEXT_ID.name, 0)

        @StringRes
        val negativeTextId : Int = args.getInt(Arg.NEGATIVE_BUTTON_TEXT_ID.name, 0)

        @StringRes
        val neutralTextId : Int = args.getInt(Arg.NEUTRAL_BUTTON_TEXT_ID.name, 0)

        /**
         *  各項目ラベル文字列リソースID
         *
         * itemLabelsより優先される
         */
        val itemLabelIds : IntArray? = args.getIntArray(Arg.ITEM_LABEL_IDS.name)

        /** 各項目ラベル文字列 */
        val itemLabels : Array<out CharSequence>? = args.getCharSequenceArray(Arg.ITEM_LABELS.name)

        /**
         * 項目の表示モード
         */
        val itemsMode : ItemsMode = args.getEnum(Arg.ITEMS_MODE.name, ItemsMode.SINGLE_CLICK)

        /** singleChoiceItemsの選択項目位置 */
        var checkedItem : Int = args.getInt(Arg.SINGLE_CHECKED_ITEM.name, 0)
            private set

        /** multiChoiceItemsの選択項目位置 */
        val checkedItems : BooleanArray by lazy {
            args.getBooleanArray(Arg.MULTI_CHECKED_ITEMS.name) ?: BooleanArray(0)
        }

        /** ボタンクリック処理後に自動でダイアログを閉じる */
        var dismissOnClickButton : Boolean = true

        /** 項目クリック処理後に自動でダイアログを閉じる (null: choiceItems時にはfalse, Items時にはtrue) */
        var dismissOnClickItem : Boolean? = null

        /** ポジティブボタンのクリック時処理 */
        var onClickPositiveButton : Listener<AlertDialogFragment>? = null

        /** ネガティブボタンのクリック時処理 */
        var onClickNegativeButton : Listener<AlertDialogFragment>? = null

        /** ニュートラルボタンのクリック時処理 */
        var onClickNeutralButton : Listener<AlertDialogFragment>? = null

        /** リスト項目のクリック時処理 */
        var onClickItem : ((AlertDialogFragment, Int)->Unit)? = null

        /** ダイアログを閉じたときの処理 */
        var onDismiss : Listener<AlertDialogFragment>? = null

        // ------ //

        fun createDialog(fragment: AlertDialogFragment) : AlertDialog {
            val builder =
                if (themeId == null) AlertDialog.Builder(fragment.requireContext())
                else AlertDialog.Builder(fragment.requireContext(), themeId)

            titleId.onNot(0) {
                builder.setTitle(it)
            }

            title?.let {
                builder.setTitle(it)
            }

            messageId.onNot(0) {
                builder.setMessage(it)
            }

            message?.let {
                builder.setMessage(it)
            }

            positiveTextId.onNot(0) {
                builder.setPositiveButton(it, null)
            }

            negativeTextId.onNot(0) {
                builder.setNegativeButton(it, null)
            }

            neutralTextId.onNot(0) {
                builder.setNeutralButton(it, null)
            }

            // 項目の初期化
            initializeItems(fragment, builder)

            val dialog = builder.show()

            dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
                onClickPositiveButton?.invoke(fragment)
                if (dismissOnClickButton) {
                    fragment.dismiss()
                }
            }

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setOnClickListener {
                onClickNegativeButton?.invoke(fragment)
                if (dismissOnClickButton) {
                    fragment.dismiss()
                }
            }

            dialog.getButton(DialogInterface.BUTTON_NEUTRAL)?.setOnClickListener {
                onClickNeutralButton?.invoke(fragment)
                if (dismissOnClickButton) {
                    fragment.dismiss()
                }
            }

            dialog.listView?.setOnItemClickListener { _, _, i, _ ->
                onClickItem?.invoke(fragment, i)
                if (false != dismissOnClickItem) {
                    fragment.dismiss()
                }
            }

            dialog.listView?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    viwe: View?,
                    i: Int,
                    l: Long
                ) {
                    checkedItem = i
                    onClickItem?.invoke(fragment, i)
                    if (true == dismissOnClickItem) {
                        fragment.dismiss()
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }

            return dialog
        }

        private fun initializeItems(
            fragment: AlertDialogFragment,
            builder: AlertDialog.Builder
        ) {
            val labels =
                itemLabelIds?.map { fragment.getText(it) }?.toTypedArray() ?: itemLabels.orEmpty()

            when (itemsMode) {
                ItemsMode.SINGLE_CLICK -> initializeSingleClickItems(builder, labels)
                ItemsMode.SINGLE_CHOICE -> initializeSingleChoiceItems(builder, labels)
                ItemsMode.MULTI_CHOICE -> initializeMultiChoiceItems(builder, labels)
            }
        }

        private fun initializeSingleClickItems(
            builder: AlertDialog.Builder,
            labels: Array<out CharSequence>
        ) {
            builder.setItems(labels, null)
        }

        private fun initializeSingleChoiceItems(
            builder: AlertDialog.Builder,
            labels: Array<out CharSequence>
        ) {
            builder.setSingleChoiceItems(labels, checkedItem, null)
        }

        private fun initializeMultiChoiceItems(
            builder: AlertDialog.Builder,
            labels: Array<out CharSequence>
        ) {
            builder.setMultiChoiceItems(labels, checkedItems, null)
        }
    }

    // ------ //

    /** 項目の表示モード */
    enum class ItemsMode {
        /** 単純に項目を列挙しクリックされたら処理を実行する */
        SINGLE_CLICK,
        /** 項目の中からひとつを選択する */
        SINGLE_CHOICE,
        /** 項目の中から複数を選択する */
        MULTI_CHOICE
    }

    // ------ //

    class Builder(
        private val styleId: Int? = null
    ) {
        private val dialog = AlertDialogFragment.createInstance()
        private val args = dialog.requireArguments().also {
            if (styleId != null) {
                it.putInt(Arg.THEME_ID.name, styleId)
            }
        }

        fun create() = dialog

        fun setTitle(@StringRes titleId: Int) : Builder {
            args.putInt(Arg.TITLE_ID.name, titleId)
            return this
        }

        fun setMessage(@StringRes messageId: Int) : Builder {
            args.putInt(Arg.MESSAGE_ID.name, messageId)
            return this
        }

        fun setTitle(title: CharSequence) : Builder {
            args.putCharSequence(Arg.TITLE.name, title)
            return this
        }

        fun setMessage(message: CharSequence) : Builder {
            args.putCharSequence(Arg.MESSAGE.name, message)
            return this
        }

        fun setPositiveButton(@StringRes textId: Int, listener: Listener<AlertDialogFragment>? = null) : Builder {
            args.putInt(Arg.POSITIVE_BUTTON_TEXT_ID.name, textId)
            dialog.setOnClickPositiveButton(listener)
            return this
        }

        fun setNegativeButton(@StringRes textId: Int, listener: Listener<AlertDialogFragment>? = null) : Builder {
            args.putInt(Arg.NEGATIVE_BUTTON_TEXT_ID.name, textId)
            dialog.setOnClickNegativeButton(listener)
            return this
        }

        fun setNeutralButton(@StringRes textId: Int, listener: Listener<AlertDialogFragment>? = null) : Builder {
            args.putInt(Arg.NEUTRAL_BUTTON_TEXT_ID.name, textId)
            dialog.setOnClickNeutralButton(listener)
            return this
        }

        fun dismissOnClickButton(flag: Boolean) : Builder {
            dialog.setDismissOnClickButton(flag)
            return this
        }

        fun dismissOnClickItem(flag: Boolean) : Builder {
            dialog.setDismissOnClickItem(flag)
            return this
        }

        // ------ //

        @Suppress("unchecked_cast")
        inline fun <reified T> setItems(
            labels: List<T>,
            noinline listener: ((dialog: AlertDialogFragment, which: Int)->Unit)? = null
        ) : Builder {
            when (T::class) {
                Int::class -> {
                    setItemsWithLabelIds(labels as List<Int>, listener)
                }

                String::class -> {
                    setItemsWithLabels(labels as List<String>, listener)
                }
            }
            return this
        }

        fun setItemsWithLabelIds(
            labelIds: List<Int>,
            listener: ((dialog: AlertDialogFragment, which: Int)->Unit)? = null
        ) : Builder {
            args.putEnum(Arg.ITEMS_MODE.name, ItemsMode.SINGLE_CLICK)
            args.putIntArray(Arg.ITEM_LABEL_IDS.name, labelIds.toIntArray())
            dialog.setOnClickItem(listener)
            return this
        }

        fun setItemsWithLabels(
            labels: List<CharSequence>,
            listener: ((dialog: AlertDialogFragment, which: Int)->Unit)? = null
        ) : Builder {
            args.putEnum(Arg.ITEMS_MODE.name, ItemsMode.SINGLE_CLICK)
            args.putCharSequenceArray(Arg.ITEM_LABELS.name, labels.toTypedArray())
            dialog.setOnClickItem(listener)
            return this
        }

        // ------ //

        @Suppress("unchecked_cast")
        inline fun <reified T> setSingleChoiceItems(
            labels: List<T>,
            checkedItem: Int,
            noinline listener: ((dialog: AlertDialogFragment, which: Int)->Unit)? = null
        ) : Builder {
            when (T::class) {
                Int::class -> {
                    setSingleChoiceItemsWithLabelIds(labels as List<Int>, checkedItem, listener)
                }

                String::class -> {
                    setSingleChoiceItemsWithLabels(labels as List<String>, checkedItem, listener)
                }
            }
            return this
        }

        fun setSingleChoiceItemsWithLabelIds(
            labelIds: List<Int>,
            checkedItem: Int,
            listener: ((dialog: AlertDialogFragment, which: Int)->Unit)? = null
        ) : Builder {
            args.putEnum(Arg.ITEMS_MODE.name, ItemsMode.SINGLE_CHOICE)
            args.putIntArray(Arg.ITEM_LABEL_IDS.name, labelIds.toIntArray())
            args.putInt(Arg.SINGLE_CHECKED_ITEM.name, checkedItem)
            dialog.setOnClickItem(listener)
            return this
        }

        fun setSingleChoiceItemsWithLabels(
            labels: List<CharSequence>,
            checkedItem: Int,
            listener: ((dialog: AlertDialogFragment, which: Int)->Unit)? = null
        ) : Builder {
            args.putEnum(Arg.ITEMS_MODE.name, ItemsMode.SINGLE_CHOICE)
            args.putCharSequenceArray(Arg.ITEM_LABELS.name, labels.toTypedArray())
            args.putInt(Arg.SINGLE_CHECKED_ITEM.name, checkedItem)
            dialog.setOnClickItem(listener)
            return this
        }
    }
}
