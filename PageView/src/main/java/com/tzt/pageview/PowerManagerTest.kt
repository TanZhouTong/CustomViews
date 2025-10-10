package com.tzt.pageview

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.view.WindowManager

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/9 16:43
 *
 * https://www.runoob.com/w3cnote/android-tutorial-powermanager.html
 */
class PowerManagerTest {
    @SuppressLint("InvalidWakeLockTag")
    private fun test(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, "test")
        wl.acquire()
        wl.release()
    }
}