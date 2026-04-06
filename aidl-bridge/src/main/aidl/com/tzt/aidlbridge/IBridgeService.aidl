// IBridgeService.aidl
package com.tzt.aidlbridge;

import com.tzt.aidlbridge.ChunkData;
import com.tzt.aidlbridge.IBridgeCallback;

// Service interface: client → server
interface IBridgeService {
    // Synchronous call (small data, must be called off the main thread)
    byte[] callSync(String method, in byte[] data);

    // Asynchronous call (small data, client returns immediately)
    oneway void callAsync(String method, in byte[] data, IBridgeCallback callback);

    // Chunked send (large data > 1MB, fire-and-forget)
    oneway void sendChunk(String method, in ChunkData chunk, IBridgeCallback callback);

    // Register for server-initiated push
    void registerCallback(String clientId, IBridgeCallback callback);

    // Unregister push callback
    void unregisterCallback(String clientId);
}
