package com.tzt.custompopupwindow

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tzt.custompopupwindow.low_attension.ExampleRecyclerViewAdapter

@SuppressLint("UseCompatLoadingForDrawables")
class CustomPopupWindow(
    private val context: Context,
    private val layoutId: Int,
    private val data: List<String>
) : PopupWindow(context) {
    private var recycleView: RecyclerView

    init {
        contentView = LayoutInflater.from(context).inflate(layoutId, null)

        height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            150f,
            context.resources.displayMetrics
        ).toInt()
        /**
         * popupWindow必须手动设置其宽高，
         * 1.如果设置了宽高就采用这里的宽高，子根布局的设置可能会忽略以及出现未知异常现象，
         * 2.如果popupWindow要适配子布局大小，可以设置为ViewGroup.LayoutParams.WRAP_CONTENT，并且要提前测量子布局的大小，在show之前应用到popupWindow中
         * */
        /*width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            200f,
            context.resources.displayMetrics
        ).toInt()*/
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        isOutsideTouchable = false
        setBackgroundDrawable(context.getDrawable(R.drawable.popup_background))

        recycleView = contentView.findViewById(R.id.content_list)
        // todo windowLayoutType

        initView()
    }

    private fun initView() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycleView.layoutManager = layoutManager


        recycleView.adapter = ExampleRecyclerViewAdapter(context, data)
    }

    override fun showAsDropDown(anchor: View?) {
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val xOffset = (contentView.measuredWidth - (anchor?.width ?: 0)) shr (1)
        Log.d("tzt", "cur_width:$width  anchor_width:${anchor?.width}")
        Log.d("tzt", "cur_width:${contentView.measuredWidth}  anchor_width:${anchor?.width}")
        Log.d("tzt", "xOffset:$xOffset")
        super.showAsDropDown(anchor, -xOffset, 20)
        /*super.showAsDropDown(anchor, 0, 0, Gravity.CENTER_VERTICAL)*/
        /*super.showAsDropDown(anchor)*/
    }
}