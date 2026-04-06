package com.tzt.customviews

import android.app.Application
import android.content.Context
import android.os.Build
import leakcanary.LeakCanary
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App: Application() {
    override fun onCreate() {
        super.onCreate()
//        LeakCanary.install(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //HiddenApiBypass 库通过 sun.misc.Unsafe.allocateInstance + 直接操作内存绕过hideApi这个限制
            HiddenApiBypass.addHiddenApiExemptions("L")
        }
    }
}