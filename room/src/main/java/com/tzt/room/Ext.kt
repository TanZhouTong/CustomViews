package com.tzt.room

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/6 15:41
 */

private val coroutineExceptionHandler: CoroutineExceptionHandler =
    CoroutineExceptionHandler { context, throwable ->
        Log.e("CoroutineExceptionHandler", "coroutineExceptionHandler -> ${throwable.message}")
        throwable.printStackTrace()
    }

val workScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + coroutineExceptionHandler)
val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + coroutineExceptionHandler)