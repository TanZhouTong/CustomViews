package com.tzt.aidlbridge.chunk

import com.tzt.aidlbridge.ChunkData
import java.util.UUID

object ChunkSplitter {

    const val CHUNK_SIZE = 512 * 1024   // 512 KB per chunk
    const val THRESHOLD = 1024 * 1024   // 1 MB trigger threshold

    /**
     * Returns true if [data] exceeds the chunking threshold.
     */
    fun needsChunking(data: ByteArray): Boolean = data.size > THRESHOLD

    /**
     * Splits [data] into a list of [ChunkData] pieces, each at most [chunkSize] bytes.
     * A random [transferId] is generated automatically.
     */
    fun split(
        data: ByteArray,
        chunkSize: Int = CHUNK_SIZE,
        transferId: String = UUID.randomUUID().toString()
    ): List<ChunkData> {
        val totalChunks = (data.size + chunkSize - 1) / chunkSize
        return (0 until totalChunks).map { index ->
            val start = index * chunkSize
            val end = minOf(start + chunkSize, data.size)
            ChunkData(
                transferId = transferId,
                chunkIndex = index,
                totalChunks = totalChunks,
                payload = data.copyOfRange(start, end)
            )
        }
    }
}
