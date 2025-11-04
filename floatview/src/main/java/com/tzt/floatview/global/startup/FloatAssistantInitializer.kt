package com.tzt.floatview.global.startup

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.WindowManager
import androidx.startup.Initializer
import com.tzt.floatview.R
import com.tzt.floatview.floatStatusDataStore
import com.tzt.floatview.proto.FloatStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/3 16:44
 */
class FloatAssistantInitializer : Initializer<FloatAssistantInitializer> {

    companion object {
        const val TAG = "FloatInitializer"
    }

    private lateinit var applicationContext: Context

    private val floatStatusFlow: Flow<FloatStatus> by lazy { applicationContext.floatStatusDataStore.data }

    /**
     * 显示状态
     */
    internal val isShowStateFlow: MutableStateFlow<Boolean> by lazy {
        MutableStateFlow<Boolean>(
            value = false
        )
    }

    override fun create(context: Context): FloatAssistantInitializer {
        applicationContext = context.applicationContext
        // floatView相关展示，对数据的收集进行相关view的处理
        MainScope().launch(Dispatchers.IO) {
            collectFloatStatusIsShow()
        }

        MainScope().launch(Dispatchers.IO) {
            collectShowStatusUi()
        }
        return this
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(DataLoadInitializer::class.java)
    }

    /**
     * 将floatStatusFlow的show状态提取为一个flow
     * */
    private suspend fun collectFloatStatusIsShow() {
        Log.w(TAG, "collectFloatStatusIsShow()...")
        floatStatusFlow.map {
            it.show
        }.collect(isShowStateFlow)
    }

    private suspend fun collectShowStatusUi() {
        if (!Settings.canDrawOverlays(applicationContext)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
            return
        }
        Log.w(TAG, "collectShowStatusUi()...")
        isShowStateFlow.collect {
            if (it) {
                // 弹窗添加view
                withContext(Dispatchers.Main) {
                    applicationContext.buildFloatView(R.layout.layout_float_view)
                }
            } else {
                // 隐藏view
            }
        }
    }
}

private fun Context.buildFloatView(resId: Int): View {
    val view = LayoutInflater.from(this).inflate(resId, null)
    // 设置悬浮窗参数
    val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_PHONE
    }
    val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        type,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.RGBA_8888
    )

    val windowManager = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
    windowManager.addView(view, params)
    view.setOnTouchListener(object : View.OnTouchListener {
        val slot = 5
        var initialX = params.x
        var initialY = params.y
        var initialTouchX = initialX
        var initialTouchY = initialY

        override fun onTouch(
            v: View,
            event: MotionEvent,
        ): Boolean {
            when (event.actionMasked) {
                ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX.toInt()
                    initialTouchY = event.rawY.toInt()
                }

                ACTION_MOVE -> {
                    val offsetX = event.rawX - initialTouchX
                    val offsetY = event.rawY - initialTouchY
                    if (abs(offsetX) > slot || abs(offsetY) > slot) {
                        windowManager.updateViewLayout(view, params.apply {
                            this.x = initialX + offsetX.toInt()
                            this.y = initialY + offsetY.toInt()
                        })
                    }
                }

                ACTION_UP -> {
                    val offsetX = event.rawX - initialTouchX
                    val offsetY = event.rawY - initialTouchY
                    if (abs(offsetX) <= slot && abs(offsetY) <= slot) {
                        view.performClick()
                    }
                }
            }
            return true
        }
    })
    return view
}