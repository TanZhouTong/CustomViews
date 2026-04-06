package com.tzt.guideview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.graphics.withSave
import androidx.core.graphics.toColorInt
import kotlin.compareTo

/**
 * @author tanzhoutong
 * @date 2026/4/6
 * description: 使用Xfermode实现镂空
 */
class GuideView(context: Context, attrSet: AttributeSet? = null, attrs: Int = 0) :
    View(context, attrSet, attrs) {

    private var holoRect: Rect? = null
    private var holoRectF: RectF? = null

    private val wm by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun setHoloRectInWindow(rect: Rect) {
        this.holoRect = rect
        this.holoRectF = RectF(
            holoRect!!.left.toFloat(),
            (holoRect!!.top - getStatusBarHeightModern()).toFloat(),
            holoRect!!.right.toFloat(),
            (holoRect!!.bottom - getStatusBarHeightModern()).toFloat()
        )
        if (this.isAttachedToWindow) {
            invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getStatusBarHeightModern(): Int {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // 获取窗口的 Insets
            rootWindowInsets?.getInsets(android.view.WindowInsets.Type.statusBars())?.top
                ?: getStatusBarHeight()
        } else {
            getStatusBarHeight()
        }
    }

    // 获取状态栏高度的工具方法
    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 使用 saveLayer 开启离屏缓冲，否则 CLEAR 模式会把背景挖成黑色
        val layerId = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.apply {
            // 1.先画背景
            drawColor("#88000000".toColorInt())

            holoRectF?.let {
                drawRoundRect(it, 5f, 5f, paint)
            }
        }

        canvas.restoreToCount(layerId)
    }
}