## 为了使镂空的位置准确，经过验证，有下属几种方法
（此处的view是从ui中点击传入的view，镂空rect的是现在需要进行的，所以需要将view的坐标转换为所需镂空位置的坐标，镂空的坐标是附加在后续window中进行绘制的）

- tag:1
- 由于WindowManager获取的window坐标默认是基于状态栏、刘海屏之下的
- 而view获取到的 getLocationInWindow 为相对当前应用的现实窗口的位置
- view获取到的 getLocationOnScreen 为相对屏幕而言的位置
- 但一般而言，app 显示的窗口都是从屏幕左上角开始，而非状态栏之下（只是对inset设置了相对于的padding）
- 所以 getLocationInWindow 与 getLocationOnScreen 在大多情况下是相同（分屏...另说）

### 1. 使用当前app的 window 作为参考系
- getWindowVisibleDisplayFrame 获取view当前所在的 window 窗口
- 此窗口Rect的上边距top包含了相关inset的边距，也就是此window是窗口的有效绘制、显示区域
- view 获取 getLocationInWindow 相对于 app window 的坐标，这个坐标不包含inset相关padding，也就是坐标是相对于最外层window而言的
- 这个获取的坐标要减去第二步获取到window的rect的左、上边距才是view真实的在绘制时的相对于parent（只有一个parent，也就是content）的坐标
- 最终拿到减去偏移的rect后进行镂空绘制

### 2. 使用WindowManager的window进行addView进行添加 view
- 获取的window坐标默认是基于状态栏、刘海屏之下的
- 传入的view绘制时的坐标便宜是相对app window的, app window（见tag:1）
- 这其中2个window的坐标转换，统一按绘制所需的坐标基准进行转换（app window）
- 由从appWindow传入的view进行坐标映射到windowManager上window的坐标，需要考虑以下几点
- 1. appWindow的边距偏移与windowManager上window的边距偏移
- 偏移的矫正可以通过以下：（如果存在时）
- a. view的坐标rect '减' statusBar 的高度得到修正后的rect
- b. windowManager在addView时，layoutParam强制对齐appWindow（flag：no_focused、in_screen、full_screen）
- c. 在b的基础上，还要考虑刘海屏的高度 layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
