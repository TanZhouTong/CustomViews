package com.tzt.floatview.global.startup

import android.content.Context
import androidx.startup.Initializer
import com.tzt.floatview.floatStatusDataStore
import com.tzt.floatview.proto.Status
import com.tzt.floatview.proto.copy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/3 17:12
 */
class DataLoadInitializer : Initializer<DataLoadInitializer> {

    override fun create(context: Context): DataLoadInitializer {
        MainScope().launch(Dispatchers.IO) {
            // 数据加载，更新相关的处理
            context.floatStatusDataStore.updateData {
                delay(3000)
                it.toBuilder().apply {
                    val old = show
                    show = !old
                    status = Status.EXPAND
                }.build()
            }
        }
        return this
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}