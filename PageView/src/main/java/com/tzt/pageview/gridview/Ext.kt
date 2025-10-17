package com.tzt.pageview.gridview

import android.graphics.Paint

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/16 20:18
 */

fun Paint.getBaseline(): Float {
    return fontMetrics.let {
        (it.descent - it.ascent) / 2 - it.descent
    }
}