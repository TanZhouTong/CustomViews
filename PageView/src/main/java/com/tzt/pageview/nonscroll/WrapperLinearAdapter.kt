package com.tzt.pageview.nonscroll

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.compareTo

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 11:08
 * 用于线性布局时的adapter,相当于每行一个item
 */
class WrapperLinearAdapter<VH : RecyclerView.ViewHolder>(
    adapter: RecyclerView.Adapter<VH>,
    private var itemsInPage: Int,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var _adapter = adapter
    var adapter: RecyclerView.Adapter<*>
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            _adapter.unregisterAdapterDataObserver(mObserver)
            value.registerAdapterDataObserver(mObserver)
            _adapter = value as RecyclerView.Adapter<VH>
            notifyDataSetChanged()
        }
        get() = _adapter

    companion object {
        private const val TYPE_WRAPPER = -1
    }

    val mObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                if (_adapter.itemCount + itemCount > this@WrapperLinearAdapter.itemCount) {
                    notifyDataSetChanged()
                } else {
                    notifyItemRangeRemoved(positionStart, itemCount)
                }
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart + itemCount < this@WrapperLinearAdapter.itemCount) {
                    notifyItemRangeChanged(positionStart, itemCount)
                } else {
                    notifyDataSetChanged()
                }
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                notifyItemRangeChanged(positionStart, itemCount)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                notifyItemRangeChanged(positionStart, itemCount, payload)
            }
        }
    }

    init {
        _adapter.registerAdapterDataObserver(mObserver)
    }

    fun finalize() {
        _adapter.unregisterAdapterDataObserver(mObserver)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_WRAPPER) {
            EmptyWrapHolder(parent.context)
        } else _adapter.onCreateViewHolder(parent, viewType)
    }

    override fun getItemCount(): Int {
        val realCount = _adapter.itemCount
        val modulo = realCount % itemsInPage
        return if (modulo == 0) realCount else realCount + itemsInPage - modulo
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= _adapter.itemCount) TYPE_WRAPPER else _adapter.getItemViewType(
            position
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == TYPE_WRAPPER) return
        @Suppress("UNCHECKED_CAST") _adapter.onBindViewHolder(holder as VH, position)
    }

    fun updateItemsInPage(itemsInPage: Int) {
        this.itemsInPage = itemsInPage
    }
}