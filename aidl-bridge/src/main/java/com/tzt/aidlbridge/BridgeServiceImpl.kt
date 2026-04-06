package com.tzt.aidlbridge

import android.os.RemoteCallbackList
import android.util.Log
import com.tzt.aidlbridge.chunk.ChunkAssembler
import com.tzt.aidlbridge.chunk.ChunkSplitter
import com.tzt.aidlbridge.ChunkData
import java.util.concurrent.Executors

private const val TAG = "BridgeServiceImpl"

/**
 * Core implementation of the AIDL service stub.
 *
 * Override [handleRequest] in a subclass to provide actual business logic.
 * The default implementation echoes the received data back to the caller.
 */
open class BridgeServiceImpl : IBridgeService.Stub() {

    private val executor = Executors.newCachedThreadPool()
    private val assembler = ChunkAssembler()

    /** Registered push callbacks keyed by clientId. */
    private val pushCallbacks = RemoteCallbackList<IBridgeCallback>()
    private val clientIdMap = HashMap<String, IBridgeCallback>()

    // -----------------------------------------------------------------------
    // IBridgeService implementation
    // -----------------------------------------------------------------------

    /**
     * Synchronous call — blocks the calling thread until the result is ready.
     * Clients MUST call this from a background thread to avoid ANR.
     */
    override fun callSync(method: String, data: ByteArray): ByteArray {
        return try {
            handleRequest(method, data)
        } catch (e: Exception) {
            Log.e(TAG, "callSync error: method=$method", e)
            ByteArray(0)
        }
    }

    /** Asynchronous call — processes on a worker thread and delivers via callback. */
    override fun callAsync(method: String, data: ByteArray, callback: IBridgeCallback?) {
        executor.execute {
            try {
                val result = handleRequest(method, data)
                deliverResult(method, result, callback)
            } catch (e: Exception) {
                Log.e(TAG, "callAsync error: method=$method", e)
                callback?.onError(method, e.message ?: "Unknown error")
            }
        }
    }

    /** Receives one chunk; fires callback when the full transfer is assembled. */
    override fun sendChunk(method: String, chunk: ChunkData, callback: IBridgeCallback?) {
        val assembled = assembler.addChunk(chunk)
        if (assembled != null) {
            executor.execute {
                try {
                    val result = handleRequest(method, assembled)
                    deliverResult(method, result, callback)
                } catch (e: Exception) {
                    Log.e(TAG, "sendChunk processing error: method=$method", e)
                    callback?.onError(method, e.message ?: "Unknown error")
                }
            }
        }
    }

    override fun registerCallback(clientId: String, callback: IBridgeCallback?) {
        if (callback == null) return
        synchronized(clientIdMap) {
            clientIdMap[clientId]?.let { old -> pushCallbacks.unregister(old) }
            pushCallbacks.register(callback)
            clientIdMap[clientId] = callback
        }
        Log.d(TAG, "registerCallback: clientId=$clientId")
    }

    override fun unregisterCallback(clientId: String) {
        synchronized(clientIdMap) {
            clientIdMap.remove(clientId)?.let { pushCallbacks.unregister(it) }
        }
        Log.d(TAG, "unregisterCallback: clientId=$clientId")
    }

    // -----------------------------------------------------------------------
    // Server-initiated push
    // -----------------------------------------------------------------------

    /**
     * Push a result to all registered clients.
     * Call from a subclass when server-side events occur.
     */
    protected fun pushToAll(method: String, data: ByteArray) {
        val count = pushCallbacks.beginBroadcast()
        try {
            if (ChunkSplitter.needsChunking(data)) {
                val chunks = ChunkSplitter.split(data)
                for (i in 0 until count) {
                    val cb = pushCallbacks.getBroadcastItem(i)
                    chunks.forEach { chunk -> cb.onChunkResult(chunk) }
                }
            } else {
                for (i in 0 until count) {
                    pushCallbacks.getBroadcastItem(i).onResult(method, data)
                }
            }
        } finally {
            pushCallbacks.finishBroadcast()
        }
    }

    // -----------------------------------------------------------------------
    // Extension point
    // -----------------------------------------------------------------------

    /**
     * Override to implement actual business logic.
     * Called on a worker thread (except for [callSync] where the caller's thread is used).
     * @return result bytes to be sent back to the client.
     */
    protected open fun handleRequest(method: String, data: ByteArray): ByteArray {
        // Default: echo
        Log.d(TAG, "handleRequest (echo): method=$method, dataSize=${data.size}")
        return data
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun deliverResult(method: String, result: ByteArray, callback: IBridgeCallback?) {
        if (callback == null) return
        if (ChunkSplitter.needsChunking(result)) {
            val chunks = ChunkSplitter.split(result)
            chunks.forEach { chunk -> callback.onChunkResult(chunk) }
        } else {
            callback.onResult(method, result)
        }
    }

    fun destroy() {
        executor.shutdownNow()
        assembler.shutdown()
        pushCallbacks.kill()
    }
}
