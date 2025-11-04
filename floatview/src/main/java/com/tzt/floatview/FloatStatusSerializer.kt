package com.tzt.floatview

import android.content.Context
import android.util.Log
import androidx.annotation.GuardedBy
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import com.tzt.floatview.proto.FloatStatus
import com.tzt.floatview.proto.Status
import com.tzt.floatview.proto.floatStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.InputStream
import java.io.OutputStream
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/3 17:42
 */

private const val TAG = "FloatStatusSerializer"

/**
 * dataStore的序列化工厂实现
 * */
val Context.floatStatusDataStore by dataStore(
    fileName = "float_view_status.db",
    serializerFactory = {
        Log.d(TAG, "serializerFactory#$it")
        FloatStatusSerializer(applicationContext = it)
    },
)

class FloatStatusSerializer(private val applicationContext: Context) : Serializer<FloatStatus> {
    override suspend fun readFrom(input: InputStream): FloatStatus {
        return runCatching {
            FloatStatus.parseFrom(input)
        }.getOrNull() ?: defaultValue
    }

    override suspend fun writeTo(t: FloatStatus, output: OutputStream) {
        runCatching {
            t.writeTo(output)
        }
    }

    override val defaultValue: FloatStatus by lazy {
        applicationContext.initDefaultStatus()
    }
}

fun Context.initDefaultStatus(): FloatStatus {
    return floatStatus {
        show = true
        status = Status.FOLD
    }
}

private fun <T> dataStore(
    fileName: String,
    serializerFactory: (Context) -> Serializer<T>,
    corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    produceMigrations: (Context) -> List<DataMigration<T>> = { listOf() },
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
): ReadOnlyProperty<Context, DataStore<T>> {
    return DataStoreSingletonDelegate(
        fileName,
        {
            Log.d(TAG, "OkioSerializerWrapper#$it")
            OkioSerializerWrapper(serializerFactory(it))
        },
        corruptionHandler,
        produceMigrations,
        scope
    )
}

/**
 * Delegate class to manage DataStore as a singleton.
 */
internal class DataStoreSingletonDelegate<T> internal constructor(
    private val fileName: String,
    private val serializerFactory: (Context) -> OkioSerializer<T>,
    private val corruptionHandler: ReplaceFileCorruptionHandler<T>?,
    private val produceMigrations: (Context) -> List<DataMigration<T>>,
    private val scope: CoroutineScope
) : ReadOnlyProperty<Context, DataStore<T>> {

    private val lock = Any()

    @GuardedBy("lock")
    @Volatile
    private var INSTANCE: DataStore<T>? = null

    /**
     * Gets the instance of the DataStore.
     *
     * @param thisRef must be an instance of [Context]
     * @param property not used
     */
    override fun getValue(thisRef: Context, property: KProperty<*>): DataStore<T> {
        return INSTANCE ?: synchronized(lock) {
            if (INSTANCE == null) {
                val applicationContext = thisRef.applicationContext
                INSTANCE = DataStoreFactory.create(
                    storage = OkioStorage(
                        FileSystem.SYSTEM,
                        serializerFactory(applicationContext)
                    ) {
                        applicationContext.dataStoreFile(fileName).absolutePath.toPath()
                    },
                    corruptionHandler = corruptionHandler,
                    migrations = produceMigrations(applicationContext),
                    scope = scope
                )
            }
            INSTANCE!!
        }
    }
}

internal class OkioSerializerWrapper<T>(private val delegate: Serializer<T>) : OkioSerializer<T> {
    override val defaultValue: T
        get() = delegate.defaultValue

    override suspend fun readFrom(source: BufferedSource): T {
        return delegate.readFrom(source.inputStream())
    }

    override suspend fun writeTo(t: T, sink: BufferedSink) {
        delegate.writeTo(t, sink.outputStream())
    }
}
