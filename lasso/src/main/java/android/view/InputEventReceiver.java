package android.view;

import android.os.Looper;

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2026/1/12 17:00
 * 这是一个存根类，仅用于骗过编译器。
 * 运行时会使用 Android 系统提供的真实实现。
 */
public abstract class InputEventReceiver {
    // 构造函数
    public InputEventReceiver(InputChannel inputChannel, Looper looper) {
    }

    // 供子类重写的方法
    public void onInputEvent(InputEvent event) {
    }

    // 必须在处理完事件后调用的 final 方法
    public final void finishInputEvent(InputEvent event, boolean handled) {
    }

    public void dispose() {
    }
}
