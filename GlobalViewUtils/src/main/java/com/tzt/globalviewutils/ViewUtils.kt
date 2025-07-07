package com.tzt.globalviewutils

import android.content.Context
import android.util.TypedValue
import android.widget.TextView

/**
 * 动态设置TextView的最大宽度
 * */
fun Context.setTextMaxWidth(textView: TextView, maxDp: Float) {
    val maxPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        maxDp,
        resources.displayMetrics
    ).toInt()
    textView.maxWidth = maxPx
}