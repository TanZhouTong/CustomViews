package com.tzt.floatview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.createBitmap

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/4 10:14
 */
class FloatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val TAG = "FloatView"
    }

    init {
        Log.w(TAG, "init()...")
    }

    val outerPaint: Paint = Paint().apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
        color = Color.GRAY
        isAntiAlias = true
    }
    val innerPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = 0x9A444444.toInt()
        isAntiAlias = true
    }

    var centerX: Float = 0f
    var centerY: Float = 0f
    var radiusInner: Float = 0f
    var radiusOuter: Float = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.w(TAG, "onSizeChanged -> w: $w, h: $h")
        centerX = w.toFloat() / 2
        centerY = h.toFloat() / 2
        radiusInner = w.coerceAtMost(h).toFloat() * 1 / 3
        radiusOuter = w.coerceAtMost(h).toFloat() / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, radiusInner, innerPaint)
        canvas.drawCircle(centerX, centerY, radiusOuter, outerPaint)
    }
}