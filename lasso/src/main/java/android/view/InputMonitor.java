package android.view;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2026/1/12 16:58
 */
public class InputMonitor implements Parcelable {
    // 隐藏方法：获取通道
    public InputChannel getInputChannel() {
        return null;
    }

    // 隐藏方法：抢夺点击事件流
    public void pilferPointers() {
    }

    // 隐藏方法：释放
    public void dispose() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<InputMonitor> CREATOR = new Parcelable.Creator<InputMonitor>() {
        public InputMonitor createFromParcel(Parcel in) {
            return null;
        }

        public InputMonitor[] newArray(int size) {
            return new InputMonitor[size];
        }
    };
}
