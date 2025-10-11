package com.tzt.pageview.nonscroll

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 13:46
 */
abstract class GridItemAdapter<T>() : BaseAdapter() {

    companion object {
        const val TAG = "GridItemAdapter"
    }

    protected val data: MutableList<T> = mutableListOf()
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): T {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        return viewBind(position = position, convertView = convertView, parent = parent)
    }

    fun submitData(data: List<*>) {
        Log.d(TAG, "submitData size:${data.size}-> data:${data[0]}")
        this.data.clear()
        this.data.addAll(data as List<T>)
        notifyDataSetChanged()
    }

    abstract fun viewBind(position: Int, convertView: View?, parent: ViewGroup): View
}