package com.tzt.lasso

import android.content.Context
import android.os.Looper
import android.util.Log
import android.view.InputChannel
import android.view.InputEvent
import android.view.InputEventReceiver
import android.view.MotionEvent
import androidx.core.content.getSystemService

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2026/1/12 16:47
 */
class ReflectionInputHelper(val context: Context) {
    private var inputMonitorObj: Any? = null
    private var receiverObj: Any? = null

    fun setup(
        displayId: Int,
        onEvent: (MotionEvent) -> Unit,
        onFinish: (Any, InputEvent, Boolean) -> Unit,
    ) {
        try {
            val inputManager = context.getSystemService(Context.INPUT_SERVICE)

            // 1. 反射调用 InputManager.monitorGestureInput
            val monitorMethod = inputManager.javaClass.getMethod(
                "monitorGestureInput", String::class.java, Int::class.java
            )
            inputMonitorObj = monitorMethod.invoke(inputManager, "LassoDetector", displayId)

            // 2. 获取 InputChannel
            val getChannelMethod = inputMonitorObj?.javaClass?.getMethod("getInputChannel")
            val inputChannel = getChannelMethod?.invoke(inputMonitorObj)

            // 3. 反射构造 InputEventReceiver 的子类
            // 注意：因为 InputEventReceiver 是抽象的，我们需要通过反射找到它的构造函数
            val receiverClass = Class.forName("android.view.InputEventReceiver")
            val inputChannelClass = Class.forName("android.view.InputChannel")

            // 我们动态创建一个匿名类（在反射层面由于无法直接 extend，通常需要在一个提前编译好的存根或者直接利用其实际存在的子类特性）
            // 在 Kotlin/Java 中，最简单的方法是写一个临时的 Java 类或者通过 Proxy（但 Proxy 只支持接口）。
            // 这里的技巧是直接使用反射构造函数。
            val constructor = receiverClass.getConstructor(inputChannelClass, Looper::class.java)
            constructor.isAccessible = true

            receiverObj = object : InputEventReceiver(
                inputChannel as InputChannel,
                Looper.getMainLooper()
            ) {
                override fun onInputEvent(event: InputEvent) {
                    if (event is MotionEvent) {
                        onEvent(event)
                    }
                    // 4. 反射调用 finishInputEvent
                    // 由于此方法在父类中，我们可以直接调用（或者反射调用以防万一）
                    onFinish(this, event, true)
                }
            }
        } catch (e: Exception) {
            Log.e("ReflectionInput", "Error setting up reflection", e)
        }
    }

    fun pilfer() {
        try {
            // 反射调用 InputMonitor.pilferPointers()
            val pilferMethod = inputMonitorObj?.javaClass?.getMethod("pilferPointers")
            pilferMethod?.invoke(inputMonitorObj)
        } catch (e: Exception) {
            Log.e("ReflectionInput", "Pilfer failed", e)
        }
    }
}