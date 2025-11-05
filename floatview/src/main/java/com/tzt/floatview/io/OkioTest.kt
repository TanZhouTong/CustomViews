package com.tzt.floatview.io

import android.content.Context
import android.os.Environment
import okio.Okio
import okio.buffer
import okio.sink
import okio.source
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/5 15:37
 */
object OkioTest {
    fun save(bytes: ByteArray) {
        val ips = ByteArrayInputStream(bytes)
        // 1.BufferedSource
        val bufferedSource = ips.source().buffer()

        // 2.BufferedSink
        val dir = Environment.getExternalStorageDirectory().toString()
        val bufferedSink = FileOutputStream("$dir/read").sink().buffer()

        // 3.write
        bufferedSink.write(bufferedSource, bytes.size.toLong())
        bufferedSink

        // or write directly
        //bufferedSink.write(bytes)
    }
}