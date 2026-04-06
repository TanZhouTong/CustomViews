package com.tzt.guideview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnComputeInternalInsetsListener(insetsListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnComputeInternalInsetsListener(insetsListener)
    }

    /**
     * 在测试机器上可行，在市场机器上需要通过反射、代理处理
     * */
    private val insetsListener = ViewTreeObserver.OnComputeInternalInsetsListener { info ->
        info.touchableRegion.setEmpty()
        info.setTouchableInsets(ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_REGION)

        // 关键点：将除了镂空区域以外的部分设为可触摸区域
        // 或者更简单：如果点击在镂空内，就不把镂空区域加入 Region
        holoRectF?.let { rectF ->
            val rect = Rect(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())

            // 创建一个全屏区域，然后减去镂空区域
            val fullRegion = Region(0, 0, width, height)
            val holeRegion = Region(rect)
            fullRegion.op(holeRegion, Region.Op.DIFFERENCE)

            info.touchableRegion.set(fullRegion)
        }
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
        /*return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // 获取窗口的 Insets
            rootWindowInsets?.getInsets(android.view.WindowInsets.Type.statusBars())?.top
                ?: getStatusBarHeight()
        } else {
            getStatusBarHeight()
        }*/
        return 0
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

    /*override fun onTouchEvent(event: MotionEvent): Boolean {
        // 如果点击发生在镂空区域，返回 false 以将事件透传给下方的 View
        if (holoRectF?.contains(event.x, event.y) == true) {
            return false
        }
        // 如果点击在阴影区域，返回 true 来拦截事件，避免误触底层
        // 如果需要在点击背景时做点什么（比如关闭引导），可以在这里处理
        return true
    }*/

    /*override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        // 1. 检查点击坐标是否在镂空区域内
        if (holoRectF?.contains(x, y) == true) {
            // 如果在镂空区域，返回 false。
            // 在 FrameLayout 等容器中，事件会自动传递给下层的 View（如被引导的按钮）。
            return false
        }

        // 2. 如果点击的是非镂空区域（即灰色阴影部分）
        // 你可以在这里处理背景点击逻辑，比如“点击背景关闭引导”
        if (event.action == MotionEvent.ACTION_UP) {
            // 如果需要，可以在这里触发关闭逻辑
            // performClick() // 建议调用以支持辅助功能
        }

        // 返回 true 表示我们消费了此事件，拦截它，不让下层接收。
        return true
    }*/

    /*override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        // 1. 检查点击坐标是否在镂空区域内
        if (holoRectF?.contains(x, y) == true) {
            // 如果在镂空区域，返回 false。
            // 在 FrameLayout 等容器中，事件会自动传递给下层的 View（如被引导的按钮）。
            return false
        }

        // 2. 如果点击的是非镂空区域（即灰色阴影部分）
        // 你可以在这里处理背景点击逻辑，比如“点击背景关闭引导”
        if (event.action == MotionEvent.ACTION_UP) {
            // 如果需要，可以在这里触发关闭逻辑
            // performClick() // 建议调用以支持辅助功能
        }

        // 返回 true 表示我们消费了此事件，拦截它，不让下层接收。
        return true
    }*/
}