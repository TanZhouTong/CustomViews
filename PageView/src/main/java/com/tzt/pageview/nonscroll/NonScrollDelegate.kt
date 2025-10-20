package com.tzt.pageview.nonscroll

import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 16:23
 */
/**
 * 不可滑动View代理类
 * @author Kevin
 * @date 01/07/2020
 */
open class NonScrollDelegate @JvmOverloads constructor(view: View? = null) {

    companion object {
        private const val TOUCH_IDLE = 0
        private const val TOUCH_DOWN = 1
        private const val TOUCH_MOVING = 2
    }

    private var downX = -1
    private var downY = -1
    private var touchType = TOUCH_IDLE
    private var touchSlop = 16 //ViewConfiguration.getWindowTouchSlop()

    init {
        if (view != null) {
            attachToView(view)
        }
    }

    fun attachToView(view: View) {
        val configuration = ViewConfiguration.get(view.context)
        touchSlop = configuration.scaledTouchSlop
        /*view.setOnTouchListener { v, event ->
            val result = onTouchEvent(v, event)
            result
        }*/
        onViewAttached(view)
    }

    protected open fun onViewAttached(view: View) {}

    open fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.toInt()
                downY = event.y.toInt()
                touchType = TOUCH_DOWN
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchType == TOUCH_MOVING) return true
                if (touchType == TOUCH_DOWN) {
                    val deltaX = abs(event.x - downX)
                    val deltaY = abs(event.y - downY)
                    if (deltaX > touchSlop || deltaY > touchSlop) {
                        touchType =
                            TOUCH_MOVING
                        return true
                    }
                } else if (touchType == TOUCH_IDLE) {
                    // 有时候没有down事件直接进入move
                    downX = event.x.toInt()
                    downY = event.y.toInt()
                    touchType = TOUCH_DOWN
                }
            }
        }
        return false
    }

    open fun onTouchEvent(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.toInt()
                downY = event.y.toInt()
                touchType = TOUCH_DOWN
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchType == TOUCH_MOVING) return true
                if (touchType == TOUCH_DOWN) {
                    val deltaX = abs(event.x - downX)
                    val deltaY = abs(event.y - downY)
                    if (deltaX > touchSlop || deltaY > touchSlop) {
                        touchType =
                            TOUCH_MOVING
                        return true
                    }
                } else if (touchType == TOUCH_IDLE) {
                    // ��ʱ��û��down�¼�ֱ��move
                    downX = event.x.toInt()
                    downY = event.y.toInt()
                    touchType = TOUCH_DOWN
                }
            }
            MotionEvent.ACTION_UP -> {
                val isMoving = touchType == TOUCH_MOVING
                touchType = TOUCH_IDLE
                return isMoving
            }
            MotionEvent.ACTION_CANCEL -> {
                touchType = TOUCH_IDLE
            }
        }
        // FlexibleGridView是一个view了，不再是ViewGroup，需要返回true
        return true
    }
}