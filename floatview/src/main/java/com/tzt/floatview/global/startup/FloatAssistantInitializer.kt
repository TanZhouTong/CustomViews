package com.tzt.floatview.global.startup

import android.content.Context
import androidx.startup.Initializer
import com.tzt.floatview.floatStatusDataStore
import com.tzt.floatview.proto.FloatStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.Boolean

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/3 16:44
 */
class FloatAssistantInitializer : Initializer<FloatAssistantInitializer> {
    private lateinit var applicationContext: Context

    private val floatStatusFlow: Flow<FloatStatus> by lazy { applicationContext.floatStatusDataStore.data }

    /**
     * 显示状态
     */
    internal val isShowStateFlow: MutableStateFlow<Boolean> by lazy {
        MutableStateFlow<Boolean>(
            value = false
        )
    }

    override fun create(context: Context): FloatAssistantInitializer {
        applicationContext = context.applicationContext
        // floatView相关展示，对数据的收集进行相关view的处理
        MainScope().launch(Dispatchers.IO) {
            collectFloatStatusIsShow()
        }

        MainScope().launch(Dispatchers.IO) {

        }
        return this
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(DataLoadInitializer::class.java)
    }

    /**
     * 将floatStatusFlow的show状态提取为一个flow
     * */
    private suspend fun collectFloatStatusIsShow() {
        floatStatusFlow.map {
            it.show
        }.collect(isShowStateFlow)
    }

    private suspend fun collectShowStatusUi() {
        isShowStateFlow.collect {
            if (it) {
                // 弹窗添加view
            } else {
                // 隐藏view
            }
        }
    }
}