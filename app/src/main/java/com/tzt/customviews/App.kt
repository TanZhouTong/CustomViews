package com.tzt.customviews

import android.app.Application
import leakcanary.LeakCanary

class App: Application() {
    override fun onCreate() {
        super.onCreate()
//        LeakCanary.install(this)
    }
}