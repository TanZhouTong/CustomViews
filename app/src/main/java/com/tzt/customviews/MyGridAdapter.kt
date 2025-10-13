/*
package com.tzt.customviews

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.tzt.pageview.nonscroll.GridItemAdapter


*/
/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 17:05
 *//*

class MyGridAdapter(val context: Context) : GridItemAdapter<ItemData>() {

    override fun viewBind(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val holder: ViewHolder
        val view: View
        //if (convertView == null) {
        view = LayoutInflater.from(context).inflate(R.layout.gridview_item, parent, false)
        holder = ViewHolder()
        holder.imageView = view.findViewById(R.id.iv_icon)
        holder.textView = view.findViewById(R.id.tv_name)
        //view.tag = holder
        */
/*} else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }*//*


        // 设置数据
        val item: ItemData = data[position]
        holder.imageView?.setImageDrawable(context.getDrawable(R.drawable.icon_shelf_show_mode_16))
        holder.textView?.text = item.name
        Log.d(TAG, "viewBind [view] is $view")
        Log.d(TAG, "viewBind [data] is ${item.name}")
        return view
    }

    class ViewHolder {
        var imageView: ImageView? = null
        var textView: TextView? = null
    }

    companion object {
        const val TAG = "MyGridAdapter"
    }
}


*/
