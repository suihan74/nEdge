package com.suihan74.utilities

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.suihan74.utilities.extensions.alsoAs

/**
 * DataBindingを使用しているリストアイテムを扱うための汎用アダプタ
 */
class BindingListAdapter<ItemT, BindingT : ViewDataBinding>(
    @LayoutRes private val itemLayoutId : Int,
    private val lifecycleOwner: LifecycleOwner,
    diffCallback : DiffUtil.ItemCallback<ItemT>,
    private val bind : (binding: BindingT, item: ItemT)->Unit
) : ListAdapter<BindingListAdapter.ViewHolderItem<ItemT>, RecyclerView.ViewHolder>(DiffCallback(diffCallback)) {

    private var onClickItem : Listener<BindingT>? = null

    private var onLongClickItem : Listener<BindingT>? = null

    private var onTouchItem : ((binding: BindingT, motionEvent: MotionEvent)->Boolean)? = null

    fun setOnClickItemListener(l : Listener<BindingT>?) {
        onClickItem = l
    }

    fun setOnLongClickItemListener(l : Listener<BindingT>?) {
        onLongClickItem = l
    }

    fun setOnTouchItemListener(l : ((binding: BindingT, motionEvent: MotionEvent)->Boolean)?) {
        onTouchItem = l
    }

    // ------ //

    override fun getItemViewType(position: Int): Int {
        return currentList[position].type.ordinal
    }

    private var headerGenerator : ((parent: ViewGroup)->View)? = null
    private var footerGenerator : ((parent: ViewGroup)->View)? = null

    @OptIn(ExperimentalStdlibApi::class)
    fun submit(
        items: List<ItemT>?,
        header: ((parent: ViewGroup)->View)? = null,
        footer: ((parent: ViewGroup)->View)? = null
    ) {
        headerGenerator = null
        footerGenerator = null

        val holders = buildList {
            if (header != null) {
                headerGenerator = header
                add(ViewHolderItem<ItemT>(type = ViewHolderType.HEADER))
            }

            addAll(
                items?.map { ViewHolderItem(type = ViewHolderType.BODY, body = it) }.orEmpty()
            )

            if (footer != null) {
                footerGenerator = footer
                add(ViewHolderItem<ItemT>(type = ViewHolderType.FOOTER))
            }
        }

        submitList(holders)
    }

    // ------ //

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder = when (viewType) {
        ViewHolderType.BODY.ordinal -> {
            val binding = DataBindingUtil.inflate<BindingT>(
                LayoutInflater.from(parent.context),
                itemLayoutId,
                parent,
                false
            ).also {
                it.lifecycleOwner = lifecycleOwner
            }

            ViewHolder(binding).also {
                it.itemView.setOnClickListener {
                    onClickItem?.invoke(binding)
                }

                it.itemView.setOnLongClickListener {
                    onLongClickItem?.invoke(binding)
                    onLongClickItem != null
                }

                it.itemView.setOnTouchListener { view, motionEvent ->
                    onTouchItem?.invoke(binding, motionEvent) ?: false
                }
            }
        }

        ViewHolderType.HEADER.ordinal -> {
            object : RecyclerView.ViewHolder(headerGenerator!!.invoke(parent)) {}
        }

        ViewHolderType.FOOTER.ordinal -> {
            object : RecyclerView.ViewHolder(footerGenerator!!.invoke(parent)) {}
        }

        else -> throw NotImplementedError()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (currentList[position].type) {
            ViewHolderType.BODY -> {
                holder.alsoAs<ViewHolder<BindingT>> {
                    bind(it.binding, currentList[position].body!!)
                }
            }

            else -> {}
        }
    }

    // ------ //

    class DiffCallback<ItemT>(
        val diffCallback: DiffUtil.ItemCallback<ItemT>
    ) : DiffUtil.ItemCallback<ViewHolderItem<ItemT>>() {
        override fun areItemsTheSame(
            oldItem: ViewHolderItem<ItemT>,
            newItem: ViewHolderItem<ItemT>
        ): Boolean {
            return oldItem.type == newItem.type &&
                    (oldItem.type != ViewHolderType.BODY || diffCallback.areItemsTheSame(oldItem.body!!, newItem.body!!))
        }

        override fun areContentsTheSame(
            oldItem: ViewHolderItem<ItemT>,
            newItem: ViewHolderItem<ItemT>
        ): Boolean {
            return oldItem.type == newItem.type &&
                    (oldItem.type != ViewHolderType.BODY || diffCallback.areContentsTheSame(oldItem.body!!, newItem.body!!))
        }
    }

    // ------ //

    class ViewHolder<BindingT : ViewDataBinding>(
        val binding : BindingT
    ) : RecyclerView.ViewHolder(binding.root)

    // ------ //

    enum class ViewHolderType {
        HEADER,
        FOOTER,
        BODY
    }

    data class ViewHolderItem<T>(
        val type : ViewHolderType,
        val body : T? = null,
    )
}
