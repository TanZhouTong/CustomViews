package com.tzt.guideview

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.collection.ArrayMap
import androidx.constraintlayout.solver.Metrics
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue

/**
 * @author tanzhoutong
 * @date 2026/4/6
 * description: todo 相关描述
 */
object GuideViewHelper {
    private const val TAG = "GuideViewHelper"
    private val guideViewsCache: ArrayMap<Long, GuideReference> = ArrayMap()
    private val weakReferenceQueue: ReferenceQueue<in GuideView> = ReferenceQueue()

    fun showGuideView(context: Context, view: View): Long {
        // 1.获取这个view的window范围
        /*val windowRect = Rect()
        view.getWindowVisibleDisplayFrame(windowRect)
        Log.i(TAG, "windowRect: $windowRect")*/

        /*val displayMetrics = context.resources.displayMetrics
        val windowRect = Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)*/

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(displayMetrics)
        val windowRect = Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)

        // 2.获取这个view的rect(相对的window)
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val locationRect = Rect().apply {
            left = location[0]
            top = location[1]
            right = location[0] + view.width
            bottom = location[1] + view.height
        }
        Log.i(TAG, "locationRect: $locationRect")

        // show
        return showGuideView(context, windowRect, locationRect)
    }

    fun hideGuideView(context: Context, key: Long) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        guideViewsCache[key]?.let {
            it.get()?.also { wm.removeView(it) } ?: run { cacheClear() }
        } ?: run {
            Log.d(TAG, "already removed")
        }
    }

    private fun showGuideView(context: Context, windowRect: Rect, locationRect: Rect): Long {
        cacheClear()

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = GuideView(context).apply {
            setHoloRectInWindow(locationRect)
        }
        /**
         * windowRect.width(),
         *                 windowRect.height(),
         *                 windowRect.left,
         *                 windowRect.top,
         * */
        wm.addView(
            view, WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.RGBA_8888
            ).apply {
                gravity = Gravity.TOP or Gravity.LEFT
                x = 0
                y = 0
            }
        )
        val timestamp = System.currentTimeMillis()
        guideViewsCache.put(timestamp, GuideReference(timestamp, view, weakReferenceQueue))
        return timestamp
    }

    /**
     * 清理cache中的数据，防止时间积累导致的内存泄漏
     * */
    private fun cacheClear() {
        var reference: Reference<*>? = null
        while (weakReferenceQueue.poll().also { reference = it } != null) {
            // 需要使用GuideReference携带的key数据，才能精确移除
            if (reference is GuideReference) {
                guideViewsCache.remove(reference.key)
                Log.i(TAG, "remove key: ${reference.key}")
            }
        }
    }

}