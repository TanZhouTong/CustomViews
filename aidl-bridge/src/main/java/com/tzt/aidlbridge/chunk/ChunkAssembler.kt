package com.tzt.aidlbridge.chunk

import com.tzt.aidlbridge.ChunkData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Thread-safe assembler that accumulates incoming chunks per [transferId].
 * Once all chunks for a transfer arrive, [addChunk] returns the reassembled data.
 * Incomplete transfers older than [timeoutSeconds] are evicted automatically.
 */
class ChunkAssembler(private val timeoutSeconds: Long = 30L) {

    private data class TransferState(
        val chunks: Array<ByteArray?>,
        val totalChunks: Int,
        var received: Int = 0,
        var timeoutFuture: ScheduledFuture<*>? = null
    )

    private val transfers = ConcurrentHashMap<String, TransferState>()
    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "chunk-assembler-gc").also { it.isDaemon = true }
    }

    /**
     * Adds one chunk. Returns the fully reassembled [ByteArray] when the transfer is complete,
     * or `null` if more chunks are still expected.
     */
    fun addChunk(chunk: ChunkData): ByteArray? {
        val state = transfers.getOrPut(chunk.transferId) {
            val newState = TransferState(
                chunks = arrayOfNulls(chunk.totalChunks),
                totalChunks = chunk.totalChunks
            )
            scheduleTimeout(chunk.transferId, newState)
            newState
        }

        synchronized(state) {
            if (state.chunks[chunk.chunkIndex] == null) {
                state.chunks[chunk.chunkIndex] = chunk.payload
                state.received++
            }
            if (state.received == state.totalChunks) {
                state.timeoutFuture?.cancel(false)
                transfers.remove(chunk.transferId)
                return assemble(state.chunks, state.totalChunks)
            }
        }
        return null
    }

    /** Manually evict a transfer (e.g. on error). */
    fun clear(transferId: String) {
        transfers.remove(transferId)?.timeoutFuture?.cancel(false)
    }

    fun shutdown() {
        scheduler.shutdownNow()
    }

    private fun scheduleTimeout(transferId: String, state: TransferState) {
        state.timeoutFuture = scheduler.schedule({
            transfers.remove(transferId)
        }, timeoutSeconds, TimeUnit.SECONDS)
    }

    private fun assemble(chunks: Array<ByteArray?>, totalChunks: Int): ByteArray {
        val totalSize = chunks.take(totalChunks).sumOf { it?.size ?: 0 }
        val result = ByteArray(totalSize)
        var offset = 0
        for (i in 0 until totalChunks) {
            val piece = chunks[i] ?: continue
            piece.copyInto(result, offset)
            offset += piece.size
        }
        return result
    }
}
