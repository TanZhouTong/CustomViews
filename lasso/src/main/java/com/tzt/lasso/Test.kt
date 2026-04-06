package com.tzt.lasso

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object Test {
    const val TAG = "TEST"
    fun testGetMetadata(context: Context) {
        //
        val packageManager = context.packageManager
        val applicationInfo =
            packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData
        Log.d(TAG, "getMetadata applicationInfo.metaData: ${applicationInfo}")

        packageManager.getProviderInfo(ComponentName(context.packageName, "todoProvider"), PackageManager.GET_META_DATA).metaData.let {
            it.getString("key01")
        }
        packageManager.getServiceInfo(ComponentName(context.packageName, "todoService"), PackageManager.GET_META_DATA).metaData.let {
            it.getString("key02")
        }
        packageManager.getActivityInfo(ComponentName(context.packageName, "todoActivity"), PackageManager.GET_META_DATA).metaData.let {
            it.getString("key03")
        }
        packageManager.getReceiverInfo(ComponentName(context.packageName, "todoReceiver"), PackageManager.GET_META_DATA).metaData.let {
            it.getString("key04")
        }
    }
}