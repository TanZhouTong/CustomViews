package android.view;

import android.graphics.Region;

public final class ViewTreeObserver {

    public void addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener listener) {
    }

    public void removeOnComputeInternalInsetsListener(OnComputeInternalInsetsListener victim) {}

    public final static class InternalInsetsInfo {

        int mTouchableInsets;

        public static final int TOUCHABLE_INSETS_REGION = 3;
        public InternalInsetsInfo() {
        }

        public final Region touchableRegion = new Region();

        public void setTouchableInsets(int val) {
            mTouchableInsets = val;
        }
    }

    public interface OnComputeInternalInsetsListener {
        /**
         * Callback method to be invoked when layout has completed and the
         * client can compute its interior insets.
         *
         * @param inoutInfo Should be filled in by the implementation with
         * the information about the insets of the window.  This is called
         * with whatever values the previous OnComputeInternalInsetsListener
         * returned, if there are multiple such listeners in the window.
         */
        public void onComputeInternalInsets(InternalInsetsInfo inoutInfo);
    }
}
