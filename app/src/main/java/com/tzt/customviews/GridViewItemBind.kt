package com.tzt.customviews

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.tzt.pageview.nonscroll.GridItemAdapter

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/13 11:07
 */
class GridViewItemBind: GridItemAdapter.IGridItemViewBind<ItemData> {
    companion object {
        const val TAG = "GridViewItemBind"
    }

    override fun viewBind(
        context: Context,
        data: List<ItemData>,
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.gridview_item, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.iv_icon)
        val textView = view.findViewById<TextView>(R.id.tv_name)
        // 设置数据
        val item: ItemData = data[position]
        imageView.setImageDrawable(context.getDrawable(R.drawable.icon_shelf_show_mode_16))
        textView.text = item.name
        Log.d(TAG, "viewBind [view] is $view")
        Log.d(TAG, "viewBind [data] is ${item.name}")
        return view
    }
}

data class ItemData(val name: String)