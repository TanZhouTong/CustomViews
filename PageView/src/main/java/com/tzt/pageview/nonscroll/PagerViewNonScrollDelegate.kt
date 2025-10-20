package com.tzt.pageview.nonscroll

import android.util.Log
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
 * 分页控件不可滑动代理
 */
class PagerViewNonScrollDelegate @JvmOverloads constructor(
    val view: View? = null,
    private var pagingTouchSlop: Int = view?.let { ViewConfiguration.get(it.context).scaledPagingTouchSlop } ?: 16
) : NonScrollDelegate(view) {

    private var downX = -1
    private var downY = -1
    private var cancel = false
    private var pagerView: PagerView? = null

    override fun onViewAttached(view: View) {
        pagerView = view as? PagerView
    }

    override fun onTouchEvent(view: View, event: MotionEvent): Boolean {
        Log.w(TAG, "onTouchEvent -> ${event.actionMasked}")
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.toInt()
                downY = event.y.toInt()
                cancel = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (cancel) {
                    return super.onTouchEvent(view, event)
                }
                // 有时候没有down事件直接进入move
                if (downX == -1 || downY == -1) {
                    downX = event.x.toInt()
                    downY = event.y.toInt()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (cancel) {
                    cancel = false
                    return super.onTouchEvent(view, event)
                }
                val deltaX = event.x - downX
                val deltaY = event.y - downY
                if (abs(deltaX) > abs(deltaY)) {
                    pageChangeIfNeed(deltaX)
                } else {
                    pageChangeIfNeed(deltaY)
                }
                downX = -1
                downY = -1
                cancel = false
            }
            MotionEvent.ACTION_CANCEL -> {
                downX = -1
                downY = -1
                cancel = false
            }
        }
        return super.onTouchEvent(view, event)
    }

    fun cancel() {
        cancel = true
    }

    /**
     * 根据手势拍段是否切页
     */
    private fun pageChangeIfNeed(delta: Float) {
        Log.d(TAG, "pageChangeIfNeed -> delta: $delta")
        if (abs(delta) > pagingTouchSlop) {
            if (delta > 0) pagerView?.previous()
            else pagerView?.next()
        }
    }

    companion object {
        const val TAG = "PagerViewNonScrollDelegate"
    }
}