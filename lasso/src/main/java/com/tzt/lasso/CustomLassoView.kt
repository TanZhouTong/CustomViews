package com.tzt.lasso

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.RectF
import android.hardware.input.InputManager
import android.os.Build
import android.os.Looper
import android.util.AttributeSet
import android.view.InputEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * @author tanzhoutong
 * @date 2026/1/12
 * description: todo 相关描述
 */
class CustomLassoView(private val context: Context) {
    private val mainScope by lazy { MainScope() }

    private val view by lazy {
        FrameLayout(context).apply {
            addView(surfaceView, FrameLayout.LayoutParams(-1, -1))
            addView(lassoView, FrameLayout.LayoutParams(-1, -1))
        }
    }

    // 1. surfaceView
    private val surfaceView: SurfaceView by lazy {
        SurfaceView(context).apply {
            holder.addCallback(SurfaceViewListener())
        }
    }

    // 2. lassoView
    private val lassoView by lazy {
        LassoView(context)
    }

    // 3. listener
    fun showLassoView() {
        initSurfaceView()
        // 设置悬浮窗参数
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE),
            PixelFormat.RGBA_8888
        )
        val windowManager = context.applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(view, params)

    }

    private fun initSurfaceView() {
        surfaceView.setZOrderMediaOverlay(true)
        surfaceView.holder.setFormat(PixelFormat.TRANSPARENT)
    }


    inner class SurfaceViewListener : SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            // 开始绘制
            mainScope.launch {
                runCatching {
                    surfaceView.holder.lockCanvas().apply {
                        drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    }
                }.getOrNull().let {
                    surfaceView.holder.unlockCanvasAndPost(it)
                }
            }
        }

        override fun surfaceChanged(
            p0: SurfaceHolder,
            p1: Int,
            p2: Int,
            p3: Int
        ) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {

        }

    }

    inner class LassoView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) : View(context, attrs, defStyleAttr, defStyleRes) {
        private val paint = createBlackWhiteDashPaints(1f, 15f)

        var path: Path? = null
            set(value) {
                field = value
                value?.let { path ->
                    RectF().also { path.computeBounds(it, false) }
                }
                invalidate()
            }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            path?.let { p ->
                canvas.drawPath(p, paint.first)
                canvas.drawPath(p, paint.second)
            }
        }
    }

    // 需要 android.permission.MONITOR_INPUT 权限
    var monitor: InputMonitor =
        InputManager.getInstance().monitorGestureInput("LassoDetector", displayId)
    var receiver: InputReceiver =
        object : InputEventReceiver(monitor.getInputChannel(), Looper.getMainLooper()) {
            public override fun onInputEvent(event: InputEvent?) {
                if (event is MotionEvent) {
                    val me = event
                    // 1. 记录轨迹坐标
                    // 2. 识别算法判断是否在“画圆”
                    if (isCircleDetected(me)) {
                        // 3. 确认为套索，抢夺焦点
                        monitor.pilferPointers()
                        switchToLassoMode()
                    }
                }
                finishInputEvent(event, false)
            }
        }

}

internal fun createBlackWhiteDashPaints(strokeWidth: Float, dashLength: Float): Pair<Paint, Paint> {
    val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        this.strokeWidth = strokeWidth
        pathEffect = DashPathEffect(floatArrayOf(dashLength, dashLength), 0f)
    }

    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        this.strokeWidth = strokeWidth
        pathEffect = DashPathEffect(floatArrayOf(dashLength, dashLength), dashLength)
    }

    return blackPaint to whitePaint
}