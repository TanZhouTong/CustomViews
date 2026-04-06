// IBridgeCallback.aidl
package com.tzt.aidlbridge;

import com.tzt.aidlbridge.ChunkData;

// Callback interface: server → client (oneway: server never blocks waiting for client)
oneway interface IBridgeCallback {
    // Small-data result
    void onResult(String method, in byte[] result);
    // Large-data chunk result
    void onChunkResult(in ChunkData chunk);
    // Error callback
    void onError(String method, String errorMessage);
}
