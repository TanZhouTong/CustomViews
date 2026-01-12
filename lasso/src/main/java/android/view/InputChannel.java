package android.view;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2026/1/12 16:56
 * 这是一个 Stub 类，用于骗过编译器。
 * 运行时将使用系统 framework.jar 中的真实类。
 */
public class InputChannel implements Parcelable {

    // 只需要提供一个空的实现，保证编译不报错
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<InputChannel> CREATOR = new Parcelable.Creator<InputChannel>() {
        public InputChannel createFromParcel(Parcel in) {
            return null;
        }

        public InputChannel[] newArray(int size) {
            return new InputChannel[size];
        }
    };

    // 如果你在代码中用到了 dispose，可以加上
    public void dispose() {}
}
