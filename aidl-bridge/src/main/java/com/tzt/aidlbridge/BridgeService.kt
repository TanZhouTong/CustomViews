package com.tzt.aidlbridge

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

private const val TAG = "BridgeService"

/**
 * The Android Service that hosts the AIDL bridge.
 *
 * Subclass this and override [createImpl] to supply your own [BridgeServiceImpl],
 * or use the default echo implementation for testing.
 *
 * Declared with `android:process=":bridge"` in the manifest to run in its own
 * process, keeping crashes isolated from the host app.
 */
open class BridgeService : Service() {

    private lateinit var impl: BridgeServiceImpl

    override fun onCreate() {
        super.onCreate()
        impl = createImpl()
        Log.d(TAG, "BridgeService created")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Client bound")
        return impl as IBinder
    }

    override fun onDestroy() {
        impl.destroy()
        super.onDestroy()
        Log.d(TAG, "BridgeService destroyed")
    }

    /** Override to provide a custom [BridgeServiceImpl]. */
    protected open fun createImpl(): BridgeServiceImpl = BridgeServiceImpl()
}
