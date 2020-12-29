package com.suihan74.utilities

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * DataBindingを使用しているリストアイテムを扱うための汎用アダプタ
 */
class BindingListAdapter<ItemT, BindingT : ViewDataBinding>(
    @LayoutRes private val itemLayoutId : Int,
    private val lifecycleOwner: LifecycleOwner,
    diffCallback : DiffUtil.ItemCallback<ItemT>,
    private val bind : (binding: BindingT, item: ItemT)->Unit
) : ListAdapter<ItemT, BindingListAdapter.ViewHolder<BindingT>>(diffCallback) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BindingT> {
        val binding = DataBindingUtil.inflate<BindingT>(
            LayoutInflater.from(parent.context),
            itemLayoutId,
            parent,
            false
        ).also {
            it.lifecycleOwner = lifecycleOwner
        }

        return ViewHolder(binding).also {
            it.itemView.setOnClickListener {
                onClickItem?.invoke(binding)
            }

            it.itemView.setOnLongClickListener {
                onLongClickItem?.invoke(binding)
                onLongClickItem != null
            }

            it.itemView.setOnTouchListener { view, motionEvent ->
                onTouchItem?.invoke(binding, motionEvent) == true
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<BindingT>, position: Int) {
        bind(holder.binding, currentList[position])
    }

    // ------ //

    class ViewHolder<BindingT : ViewDataBinding>(
        val binding : BindingT
    ) : RecyclerView.ViewHolder(binding.root)
}
