package com.tzt.aidlbridge

import android.os.Parcel
import android.os.Parcelable

/**
 * Represents one chunk of a large data transfer across Binder.
 * Must be in package com.tzt.aidlbridge to match the AIDL parcelable declaration.
 */
class ChunkData(
    val transferId: String,
    val chunkIndex: Int,
    val totalChunks: Int,
    val payload: ByteArray
) : Parcelable {

    constructor(parcel: Parcel) : this(
        transferId = parcel.readString() ?: "",
        chunkIndex = parcel.readInt(),
        totalChunks = parcel.readInt(),
        payload = parcel.createByteArray() ?: ByteArray(0)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(transferId)
        parcel.writeInt(chunkIndex)
        parcel.writeInt(totalChunks)
        parcel.writeByteArray(payload)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ChunkData> {
        override fun createFromParcel(parcel: Parcel): ChunkData = ChunkData(parcel)
        override fun newArray(size: Int): Array<ChunkData?> = arrayOfNulls(size)
    }
}
