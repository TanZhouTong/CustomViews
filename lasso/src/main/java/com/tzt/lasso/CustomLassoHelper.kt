package com.tzt.lasso

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.RectF
import android.hardware.input.InputManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.InputEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.compareTo
import kotlin.math.abs
import kotlin.text.compareTo


/**
 * @author tanzhoutong
 * @date 2026/1/12
 * description: todo 相关描述
 */
class CustomLassoHelper(private val context: Context) {
    companion object {
        const val TAG = "CustomLassoHelper"
    }

    private val mainScope by lazy { MainScope() }
    private val renderScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    private val view by lazy {
        FrameLayout(context).apply {
            addView(surfaceView, FrameLayout.LayoutParams(-1, -1))
            addView(lassoView, FrameLayout.LayoutParams(-1, -1))
        }
    }

    // 1. surfaceView
    private val surfaceView: SurfaceView by lazy {
        SurfaceView(context).apply {
            SurfaceViewListener().let {
                holder.addCallback(it)
                setOnTouchListener(it)
            }
        }
    }

    private val realtimePen by lazy {
        createBlackWhiteDashPaints(1f, 15f)
    }

    // 2. lassoView
    private val lassoView by lazy {
        LassoView(context)
    }

    private val manager by lazy {
        context.applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
    }

    // 3. listener
    fun showLassoView() {
        Log.d(TAG, "showLassoView")
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName)
            )
            // 这里当前还在contentProvider启动阶段，这里无效
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            initSurfaceView()
            // 设置悬浮窗参数
            manager.addView(view, generateParam(false))
            view.post { setupInputMonitor(view.rootView) }
        }
    }

    private fun initSurfaceView() {
        Log.d(TAG, "initSurfaceView")
        surfaceView.setZOrderMediaOverlay(true)
        surfaceView.setZOrderOnTop(true)
        surfaceView.holder.setFormat(PixelFormat.TRANSPARENT)
    }

    private fun drawToSurface(path: Path) {
        renderScope.launch {
            runCatching {
                surfaceView.holder.lockCanvas().apply {
                    drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    drawPath(path, realtimePen.first)
                    drawPath(path, realtimePen.second)
                }
            }.getOrNull()?.let {
                surfaceView.holder.unlockCanvasAndPost(it)
            }
        }
    }


    inner class SurfaceViewListener : SurfaceHolder.Callback, View.OnTouchListener {
        var tempPath: Path? = null
        override fun surfaceCreated(p0: SurfaceHolder) {
            // 开始绘制
            mainScope.launch(Dispatchers.IO) {
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
            p3: Int,
        ) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {

        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(
            v: View,
            event: MotionEvent,
        ): Boolean {
            val toolType = event.getToolType(event.actionIndex)
            if (toolType != MotionEvent.TOOL_TYPE_STYLUS && toolType != MotionEvent.TOOL_TYPE_MOUSE) {
                switchToLassoMode(false)
                return false
            }
            switchToLassoMode(true)
            return when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lassoView.path = null
                    val path = tempPath ?: Path().also { tempPath = it }
                    event.drawPath(path)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val path = tempPath ?: Path().also { tempPath = it }
                    event.drawPath(path)
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val path = tempPath ?: Path().also { tempPath = it }
                    event.drawPath(path)
                    tempPath?.close()
                    tempPath = null
                    true
                }

                else -> false
            }
        }
    }

    private var lastX = 0f
    private var lastY = 0f
    private val touchSlop = 2f // 只有移动超过2像素才重绘

    private fun MotionEvent.drawPath(path: Path) {
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            path.moveTo(x, y)
        } else {
            if (historySize > 0) {
                for (i in 0 until historySize) {
                    val x = getHistoricalX(0, i)
                    val y = getHistoricalY(0, i)
                    if (abs(x - lastX) > touchSlop || abs(y - lastY) > touchSlop) {
                        lastX = x
                        lastY = y
                        path.lineTo(x, y)
                        drawToSurface(path)
                    }
                }
            } else {
                if (abs(x - lastX) > touchSlop || abs(y - lastY) > touchSlop) {
                    lastX = x
                    lastY = y
                    path.lineTo(x, y)
                    drawToSurface(path)
                }
            }
            // 关键：实时同步给 SurfaceView 绘制
        }
        // 同步给 lassoView（用于 UP 后的静态显示）
        if (actionMasked == MotionEvent.ACTION_CANCEL || actionMasked == MotionEvent.ACTION_UP) {
            lassoView.path = path
        }
    }

    inner class LassoView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0,
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
    /*var monitor: InputMonitor =
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
        }*/

    private val focus: AtomicBoolean = AtomicBoolean(false)

    private fun switchToLassoMode(_focus: Boolean) {
        if (focus.compareAndSet(!_focus, _focus)) {
            manager.updateViewLayout(view, generateParam(_focus))
        }
    }

    private fun generateParam(focusable: Boolean): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            if (focusable) {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            } else {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            },
            PixelFormat.RGBA_8888
        )
    }

    // 在 CustomLassoHelper 内部
    private var reflectionHelper: ReflectionInputHelper? = null

    private fun setupInputMonitor(view: View) {
        val dId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.display.displayId
        } else {
            0
        }

        reflectionHelper = ReflectionInputHelper(context)
        reflectionHelper?.setup(
            dId,
            onEvent = { event ->
                handleGlobalMotionEvent(event)
            },
            onFinish = { receiver, event, handled ->
                // 反射调用 finishInputEvent(event, handled)
                try {
                    val method = receiver::class.java.superclass.getDeclaredMethod(
                        "finishInputEvent", InputEvent::class.java, Boolean::class.java
                    )
                    // 这里需要注意，如果是直接继承，receiver 此时就是 InputEventReceiver
                    method.invoke(receiver, event, handled)
                } catch (e: Exception) {
                    // 如果上面报错，直接强制转型（因为编译时其实能找到这个类，只是报错）
                    (receiver as? android.view.InputEventReceiver)?.finishInputEvent(event, handled)
                }
            }
        )
    }

    var tempPath: Path? = null
    private fun handleGlobalMotionEvent(event: MotionEvent) {
        // ... 你的判断逻辑 ...
        if (event.shouldIntercept()) {
            reflectionHelper?.pilfer() // 执行抢夺
            switchToLassoMode(true)    // 修改 WindowManager Flag
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lassoView.path = null
                    val path = tempPath ?: Path().also { tempPath = it }
                    event.drawPath(path)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val path = tempPath ?: Path().also { tempPath = it }
                    event.drawPath(path)
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val path = tempPath ?: Path().also { tempPath = it }
                    event.drawPath(path)
                    tempPath?.close()
                    tempPath = null
                    switchToLassoMode(false)
                    true
                }

                else -> false
            }
        } else {
            switchToLassoMode(false)
        }
    }

    private fun MotionEvent.shouldIntercept(): Boolean {
        val toolType = getToolType(actionIndex)
        return toolType == MotionEvent.TOOL_TYPE_STYLUS || toolType == MotionEvent.TOOL_TYPE_MOUSE
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