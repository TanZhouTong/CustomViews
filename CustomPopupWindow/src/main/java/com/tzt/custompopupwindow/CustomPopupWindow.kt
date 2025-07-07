package com.tzt.custompopupwindow

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
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
        val xOffset = (width - (anchor?.width ?: 0)) shr (1)
        super.showAsDropDown(anchor, xOffset, 2)
    }
}