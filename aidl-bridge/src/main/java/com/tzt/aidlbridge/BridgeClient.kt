package com.tzt.aidlbridge

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.tzt.aidlbridge.chunk.ChunkAssembler
import com.tzt.aidlbridge.chunk.ChunkSplitter
import com.tzt.aidlbridge.ChunkData
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "BridgeClient"

/**
 * Helper class that manages binding to [BridgeService] and exposes
 * sync / async / large-data call APIs.
 *
 * @param servicePackage  Package name of the host app that exports the service.
 * @param serviceClass    Fully-qualified class name of the [BridgeService] subclass.
 */
class BridgeClient(
    private val servicePackage: String,
    private val serviceClass: String
) {
    private var service: IBridgeService? = null
    private var onConnectedCallback: (() -> Unit)? = null

    /** Assembler for large results returned by the server. */
    private val resultAssembler = ChunkAssembler()

    /** Pending async callbacks keyed by method (simple approach). */
    private val pendingCallbacks =
        ConcurrentHashMap<String, Pair<(ByteArray) -> Unit, (String) -> Unit>>()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = IBridgeService.Stub.asInterface(binder)
            Log.d(TAG, "Connected to $name")
            onConnectedCallback?.invoke()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            Log.w(TAG, "Disconnected from $name")
        }
    }

    private val remoteCallback = object : IBridgeCallback.Stub() {
        override fun onResult(method: String, result: ByteArray) {
            pendingCallbacks.remove(method)?.first?.invoke(result)
        }

        override fun onChunkResult(chunk: ChunkData) {
            val assembled = resultAssembler.addChunk(chunk) ?: return
            // Use transferId as method key is not available here; clients can
            // differentiate by registering per-method callbacks before large calls.
            pendingCallbacks.remove(chunk.transferId)?.first?.invoke(assembled)
        }

        override fun onError(method: String, errorMessage: String) {
            pendingCallbacks.remove(method)?.second?.invoke(errorMessage)
        }
    }

    // -----------------------------------------------------------------------
    // Connection lifecycle
    // -----------------------------------------------------------------------

    fun connect(context: Context, onConnected: () -> Unit) {
        onConnectedCallback = onConnected
        val intent = Intent().apply {
            setClassName(servicePackage, serviceClass)
        }
        val bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        if (!bound) {
            Log.e(TAG, "bindService returned false — is the service declared in the manifest?")
        }
    }

    fun disconnect(context: Context) {
        resultAssembler.shutdown()
        try {
            context.unbindService(connection)
        } catch (_: IllegalArgumentException) {
            // Not bound
        }
        service = null
    }

    // -----------------------------------------------------------------------
    // Sync call (coroutine wrapper — must be called from a non-main dispatcher)
    // -----------------------------------------------------------------------

    /**
     * Blocking synchronous call. Wrap in `withContext(Dispatchers.IO)`.
     * Throws [IllegalStateException] if not connected.
     */
    suspend fun callSync(method: String, data: ByteArray): ByteArray =
        suspendCancellableCoroutine { cont ->
            val svc = requireService()
            try {
                val result = svc.callSync(method, data)
                cont.resume(result)
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }

    // -----------------------------------------------------------------------
    // Async call
    // -----------------------------------------------------------------------

    fun callAsync(
        method: String,
        data: ByteArray,
        onResult: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        pendingCallbacks[method] = onResult to onError
        try {
            requireService().callAsync(method, data, remoteCallback)
        } catch (e: Exception) {
            pendingCallbacks.remove(method)
            onError(e.message ?: "Unknown error")
        }
    }

    // -----------------------------------------------------------------------
    // Large-data call (auto-chunking)
    // -----------------------------------------------------------------------

    /**
     * Automatically splits data > 1 MB into 512 KB chunks and sends them via
     * [IBridgeService.sendChunk]. For smaller data, falls back to [callAsync].
     *
     * For large responses: the server will reply with multiple [onChunkResult]
     * callbacks. The client needs to register [onResult] keyed by the [transferId]
     * used for the chunks. This is handled transparently by [remoteCallback].
     */
    fun callLargeData(
        method: String,
        data: ByteArray,
        onResult: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!ChunkSplitter.needsChunking(data)) {
            callAsync(method, data, onResult, onError)
            return
        }

        val transferId = UUID.randomUUID().toString()
        // Register callback keyed by transferId so large results can be routed back.
        pendingCallbacks[transferId] = onResult to onError

        val chunks = ChunkSplitter.split(data, transferId = transferId)
        val svc = try {
            requireService()
        } catch (e: IllegalStateException) {
            pendingCallbacks.remove(transferId)
            onError(e.message ?: "Not connected")
            return
        }

        try {
            chunks.forEach { chunk -> svc.sendChunk(method, chunk, remoteCallback) }
        } catch (e: Exception) {
            pendingCallbacks.remove(transferId)
            onError(e.message ?: "Unknown error")
        }
    }

    // -----------------------------------------------------------------------
    // Push registration
    // -----------------------------------------------------------------------

    /**
     * Register to receive server-initiated push messages.
     * @param clientId  Unique identifier for this client (e.g. UUID or app package name).
     * @param onPush    Invoked on the Binder thread when the server pushes small data.
     * @param onChunk   Invoked on the Binder thread for each chunk of a large push.
     *                  Use [ChunkAssembler] separately if you need reassembly.
     */
    fun registerPush(
        clientId: String,
        onPush: (method: String, data: ByteArray) -> Unit,
        onChunk: (ChunkData) -> Unit,
        onError: (method: String, msg: String) -> Unit
    ) {
        val cb = object : IBridgeCallback.Stub() {
            override fun onResult(method: String, result: ByteArray) = onPush(method, result)
            override fun onChunkResult(chunk: ChunkData) = onChunk(chunk)
            override fun onError(method: String, errorMessage: String) = onError(method, errorMessage)
        }
        try {
            requireService().registerCallback(clientId, cb)
        } catch (e: Exception) {
            Log.e(TAG, "registerPush failed", e)
        }
    }

    fun unregisterPush(clientId: String) {
        try {
            requireService().unregisterCallback(clientId)
        } catch (e: Exception) {
            Log.e(TAG, "unregisterPush failed", e)
        }
    }

    // -----------------------------------------------------------------------

    private fun requireService(): IBridgeService =
        service ?: throw IllegalStateException("BridgeClient is not connected. Call connect() first.")
}
