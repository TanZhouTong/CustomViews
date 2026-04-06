package com.tzt.customviews

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tzt.aidlbridge.BridgeClient
import com.tzt.aidlbridge.chunk.ChunkAssembler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AidlTestActivity"

/**
 * Manual verification activity for the aidl-bridge SDK.
 *
 * Tests:
 *  1. Bind to BridgeService
 *  2. callSync — small data, must run on IO thread
 *  3. callAsync — callback delivery
 *  4. callLargeData — 2 MB payload, automatic chunk split + reassembly
 *  5. registerPush — server-initiated push
 */
class AidlTestActivity : AppCompatActivity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // The service runs in the SAME app for demo purposes.
    // In a real multi-app scenario, replace with the remote app's package/class.
    private val client = BridgeClient(
        servicePackage = packageName,
        serviceClass = "com.tzt.aidlbridge.BridgeService"
    )

    private lateinit var tvLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aidl_test)
        tvLog = findViewById(R.id.tv_log)

        client.connect(this) {
            log("Connected to BridgeService")
        }

        findViewById<Button>(R.id.btn_sync).setOnClickListener { testSync() }
        findViewById<Button>(R.id.btn_async).setOnClickListener { testAsync() }
        findViewById<Button>(R.id.btn_large).setOnClickListener { testLargeData() }
        findViewById<Button>(R.id.btn_push).setOnClickListener { testPush() }
    }

    override fun onDestroy() {
        client.unregisterPush(packageName)
        client.disconnect(this)
        super.onDestroy()
    }

    // ------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------

    private fun testSync() {
        scope.launch {
            val payload = "Hello sync".toByteArray()
            val result = withContext(Dispatchers.IO) {
                runCatching { client.callSync("echo", payload) }
            }
            result.fold(
                onSuccess = { log("callSync OK: ${it.decodeToString()}") },
                onFailure = { log("callSync ERROR: ${it.message}") }
            )
        }
    }

    private fun testAsync() {
        val payload = "Hello async".toByteArray()
        client.callAsync(
            method = "echo",
            data = payload,
            onResult = { log("callAsync OK: ${it.decodeToString()}") },
            onError = { log("callAsync ERROR: $it") }
        )
    }

    private fun testLargeData() {
        // Generate a 2 MB payload
        val twoMb = ByteArray(2 * 1024 * 1024) { it.toByte() }
        log("Sending 2 MB payload …")

        // The server echoes the data back. Large results arrive as chunks.
        // We use a local assembler to reconstruct the response.
        val responseAssembler = ChunkAssembler()

        client.callLargeData(
            method = "echo",
            data = twoMb,
            onResult = { result ->
                log("callLargeData OK: received ${result.size} bytes, match=${result.contentEquals(twoMb)}")
                responseAssembler.shutdown()
            },
            onError = { log("callLargeData ERROR: $it") }
        )
    }

    private fun testPush() {
        client.registerPush(
            clientId = packageName,
            onPush = { method, data ->
                runOnUiThread { log("Push received: method=$method, size=${data.size}") }
            },
            onChunk = { chunk ->
                Log.d(TAG, "Push chunk: ${chunk.chunkIndex + 1}/${chunk.totalChunks}")
            },
            onError = { method, msg ->
                runOnUiThread { log("Push error: method=$method, msg=$msg") }
            }
        )
        log("Registered push callback for clientId=$packageName")
    }

    // ------------------------------------------------------------------

    private fun log(msg: String) {
        Log.d(TAG, msg)
        tvLog.append("$msg\n")
    }
}
