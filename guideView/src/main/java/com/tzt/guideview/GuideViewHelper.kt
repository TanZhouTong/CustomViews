package com.tzt.guideview

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.collection.ArrayMap
import androidx.constraintlayout.solver.Metrics
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import kotlin.text.compareTo

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
        val windowRectOwner = Rect()
        view.getWindowVisibleDisplayFrame(windowRectOwner)
        Log.i(TAG, "windowRectOwner: $windowRectOwner")

        /*val displayMetrics = context.resources.displayMetrics´´
        val windowRect = Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)*/

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(displayMetrics)
        val windowRect = Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        Log.i(TAG, "windowRect: $windowRect")

        // 2.获取这个view的rect(相对的window)
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val locationRect = Rect().apply {
            left = location[0] - windowRectOwner.left
            top = location[1] - windowRectOwner.top
            right = left + view.width
            bottom = top + view.height
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
        wm.addView(
            view, WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.RGBA_8888
            ).apply {
                gravity = Gravity.TOP or Gravity.LEFT
                x = 0
                y = 0

                // 4. 关键：解决刘海屏（Notch）导致的偏移
                // 如果不设置这个，在有刘海的手机上，系统会自动把整个窗口压下刘海的高度
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
//                }
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