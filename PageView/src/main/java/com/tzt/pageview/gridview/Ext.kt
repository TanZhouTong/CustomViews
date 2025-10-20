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

fun String.clipMeasureInfo(paint: Paint, validWidth: Float): MeasureInfo {
    val addition = "..."
    var result = this
    var rbTextWidth = paint.measureText(result)
    run outer@{
        while (rbTextWidth >= validWidth) {
            result = result.trimEnd('.').let {
                if (it.isEmpty()) {
                    addition
                    return@outer
                } else it.substring(0, it.length - 1) + addition
            }
            rbTextWidth = paint.measureText(result)
        }
    }
    return MeasureInfo(result, rbTextWidth)
}


data class MeasureInfo(val value: String, val measureWidth: Float)