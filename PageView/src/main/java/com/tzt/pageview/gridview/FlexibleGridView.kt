package com.tzt.pageview.gridview

import android.annotation.SuppressLint
import android.content.Context
import android.database.Observable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.tzt.pageview.R
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toRect
import androidx.core.graphics.withSave
import com.tzt.pageview.nonscroll.ListPager
import com.tzt.pageview.nonscroll.PagerView
import com.tzt.pageview.nonscroll.Utils
import kotlin.math.abs

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/16 13:53
 */
class FlexibleGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr), PagerView {

    companion object {
        const val INVALID = -1
        const val TAG = "FlexibleGridView"
    }

    /**
     * 后续去掉得了
     * */
    val mObserver: FlexibleGridDataSetObserver = object : FlexibleGridDataSetObserver() {
        override fun onChanged() {
            reloadUiWithDataChange()
        }

        override fun onPositionChanged(position: Int) {
            reloadUiWithDataChange(position)
        }

        override fun onConfigurationChange(oldItemsInPage: Int) {
            reloadUiWithConfigurationChange(oldItemsInPage, reSort = false)
        }
    }

    val itemRectCache: MutableList<RectF> = mutableListOf()     // 各子item的rect
    val coverRectCache: MutableList<RectF> = mutableListOf()    // 子item cover背景的rect
    val rtRectCache: MutableList<RectF> = mutableListOf()       // 子item 右上角预留的rect，绘制bitmap
    val rbRectCache: MutableList<RectF> = mutableListOf()       // 子item 右下角rect，用户文件状态描述
    val ltRectCache: MutableList<RectF> = mutableListOf()       // 子item 左上角预留的rect，内容待定

    // attrs
    var coverWidthAttr: Float = 0f
    var coverHeightAttr: Float = 0f
    var rtSizeAttr: Float = 0f
    var itemMinXGap: Float = 0f
    var itemMinYGap: Float = 0f
    var itemCornerDimension: Float = 0f
    var rtMarginTopDimension: Float = 0f
    var rtMarginRightDimension: Float = 0f
    var rbTextSizeDimension: Float = 0f
    var rbMarginVerticalDimension: Float = 0f
    var rbMarginHorizontalDimension: Float = 0f
    var rbBackgroundColor: Int = 0  // 背景色
    var rbTitleTextColor: Int = 0 // 前景色
    var titleTextSizeDimension: Float = 0f
    var titleTextAlignment: Int = 0

    // property
    var itemWidth = 0f
    var itemHeight = 0f
    var coverWidth = 0f
    var coverHeight = 0f

    var rtSize = 0f

    var xGap = 0f
    var yGap = 0f

    // text 基线，相对于item的distance
    var rbBaseline = 0f
    var titleBaseline = 0f

    // tool
    lateinit var strokePaint: Paint
    lateinit var rbTextPaint: Paint
    lateinit var titlePaint: Paint
    lateinit var bitmapPaint: Paint
    lateinit var rtBitmapPaint: Paint
    lateinit var rbBackgroundPaint: Paint

    lateinit var canvasCache: Canvas    // 离屏canvas
    lateinit var cacheBitmap: Bitmap    // 离屏canvas中bitmap

    var scaleMode = ScaleMode.ObeyPosition

    init {
        // 这里获取相关属性数据 attrs解析
        init(attrs)
    }

    @SuppressLint("PrivateResource")
    private fun init(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.FlexibleGridView) {
            // item
            coverWidthAttr = getDimension(
                R.styleable.FlexibleGridView_coverWidth,
                context.resources.getDimension(R.dimen.flexible_grid_view_default_item_width)
            )
            coverHeightAttr = getDimension(
                R.styleable.FlexibleGridView_coverHeight,
                context.resources.getDimension(R.dimen.flexible_grid_view_default_item_height)
            )
            itemMinXGap = getDimension(R.styleable.FlexibleGridView_itemHorizontalMinGap, 0f)
            itemMinYGap = getDimension(R.styleable.FlexibleGridView_itemVerticalMinGap, 0f)
            isLooper = getBoolean(R.styleable.FlexibleGridView_circle, true)
            itemCornerDimension = getDimension(
                R.styleable.FlexibleGridView_corner,
                context.resources.getDimension(R.dimen.flexible_grid_view_default_corner_size)
            )
            // rt
            rtSizeAttr = getDimension(
                R.styleable.FlexibleGridView_rtSize,
                context.resources.getDimension(R.dimen.flexible_grid_view_default_rt_size)
            )
            rtMarginTopDimension = getDimension(R.styleable.FlexibleGridView_rtMarginTop, 0f)
            rtMarginRightDimension = getDimension(R.styleable.FlexibleGridView_rtMarginRight, 0f)
            // rb
            rbTextSizeDimension = getDimension(
                R.styleable.FlexibleGridView_rbTextSize,
                context.resources.getDimension(R.dimen.flexible_grid_view_default_rb_text_size)
            )
            rbMarginHorizontalDimension =
                getDimension(R.styleable.FlexibleGridView_rbMarginHorizontal, 0f)
            rbMarginVerticalDimension =
                getDimension(R.styleable.FlexibleGridView_rbMarginVertical, 0f)
            rbBackgroundColor = getColor(
                R.styleable.FlexibleGridView_rbBackgroundColor,
                context.resources.getColor(R.color.opacity_gray, null)
            )
            rbTitleTextColor = getColor(R.styleable.FlexibleGridView_rbForegroundColor, Color.WHITE)
            // title
            titleTextSizeDimension = getDimension(
                R.styleable.FlexibleGridView_titleTextSize,
                context.resources.getDimension(R.dimen.flexible_grid_view_default_title_text_size)
            )
            // 默认靠左
            titleTextAlignment = getInteger(R.styleable.FlexibleGridView_titleTextAlign, 0)
        }

        // paint
        strokePaint = Paint().apply {
            strokeWidth = 1f
            color = Color.BLACK
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        rbTextPaint = Paint().apply {
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
            color = rbTitleTextColor
            textSize = rbTextSizeDimension
        }
        rbBackgroundPaint = Paint().apply {
            isAntiAlias = true
            color = rbBackgroundColor
            style = Paint.Style.FILL
        }
        titlePaint = Paint().apply {
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
            color = Color.GRAY
            textSize = titleTextSizeDimension
        }
        bitmapPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            isFilterBitmap = true
        }
        rtBitmapPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            isFilterBitmap = true
        }

        isClickable = true
        isLongClickable = true
    }

    // sizeChange时测量各rect绘制区域,
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d(TAG, "onSizeChanged -> width: $w, height: $h")
        // 保留所测量的item相关size数据，字体相关size数据
        // view初次创建时adapter还没有，直接返回，后续size变化时会触发
        if (adapter == null || adapter!!.itemsInPage <= 0) {
            Log.w(
                TAG,
                "onSizeChanged() error with -> adapter: $adapter, rows: $rows, columns: $columns"
            )
            return
        }
        // 计算rect
    }

    // 绘制，需要考虑： 1.背景封面 2.右上角图标 3.文字 4.后续可能需要拓展的view部分
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 通过rect开始绘制数据
        if (itemRectCache.isEmpty()) {
            // 如果item容量为0，就直接绘制白屏返回吧
            canvas.drawColor(Color.WHITE)
        } else {
            //
            canvasCache = Canvas(createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
                cacheBitmap = it
            })
            adapter?.getCurrentPageData(currentPage)?.forEachIndexed { index, item ->
                drawGridItem(index)
            }
            canvas.drawBitmap(cacheBitmap, 0f, 0f, bitmapPaint)
        }
    }

    private fun drawGridItem(index: Int) {
        val itemRect = itemRectCache[index]
        canvasCache.withSave {
            clipRect(itemRect)
            // 1. draw 背景
            val coverRect = coverRectCache[index]
            canvasCache.withSave {
                val path = Path().apply {
                    addRoundRect(
                        coverRect,
                        itemCornerDimension,
                        itemCornerDimension,
                        Path.Direction.CW
                    )
                }
                clipPath(path)
                drawBitmap(
                    adapter!!.getCoverBitmap(getRealPosition(index), coverWidth, coverHeight),
                    null,
                    coverRect,
                    bitmapPaint
                )
            }

            // 2. draw 外框
            canvasCache.drawRoundRect(
                coverRect,
                itemCornerDimension,
                itemCornerDimension,
                strokePaint
            )

            // 3. 画右上角select
            val rtRect = rtRectCache[index]
            val rtConfig = adapter!!.getRtConfig(getRealPosition(index))
            if (rtConfig.need && rtConfig.src != null) {
                canvasCache.drawBitmap(rtConfig.src, null, rtRect, rtBitmapPaint)
            }
            // 4. 画rb下载状态
            val rbRect = rbRectCache[index]
            val rbContent = adapter!!.getProgressDescriptions(getRealPosition(index))
            if (rbContent.isNotEmpty()) {
                // 4.1 先画背景
                val rbTextWidth = rbTextPaint.measureText(rbContent)
                val validWidth = rbRect.width() - 2 * rbMarginHorizontalDimension
                var isFull = rbTextWidth >= validWidth
                val clipMeasureInfo = rbContent.clipMeasureInfo(rbTextPaint, validWidth)
                val textLeft =
                    rbRect.width() - clipMeasureInfo.measureWidth - rbMarginHorizontalDimension
                val backgroundRect =
                    RectF(
                        if (isFull) rbRect.left else rbRect.left + textLeft - rbMarginHorizontalDimension,
                        rbRect.top,
                        rbRect.right,
                        rbRect.bottom
                    )
                Path().apply {
                    addRoundRect(
                        backgroundRect,
                        floatArrayOf(
                            0f,
                            0f,
                            0f,
                            0f,
                            itemCornerDimension,
                            itemCornerDimension,
                            if (isFull) itemCornerDimension else 0f,
                            if (isFull) itemCornerDimension else 0f
                        ),
                        Path.Direction.CW
                    )
                    canvasCache.drawPath(this, rbBackgroundPaint)
                }
                // 4.2 再画字体
                canvasCache.drawText(
                    clipMeasureInfo.value,
                    rbRect.left + textLeft,
                    itemRect.top + rbBaseline,
                    rbTextPaint
                )
            }
            // 5. 画title字体
            val titleClipMeasureInfo = adapter!!.getTitleText(getRealPosition(index))
                .clipMeasureInfo(titlePaint, itemRect.width())
            val leftDistance = when (titleTextAlignment) {
                0 -> { // left
                    0f
                }

                1 -> { // center
                    (itemRect.width() - titleClipMeasureInfo.measureWidth) / 2f
                }

                2 -> { // right
                    itemRect.width() - titleClipMeasureInfo.measureWidth
                }

                else -> throw IllegalArgumentException("unknown attr value: $titleTextAlignment of FlexibleGridView_titleTextAlign")
            }
            canvasCache.drawText(
                titleClipMeasureInfo.value,
                itemRect.left + leftDistance,
                itemRect.top + titleBaseline,
                titlePaint
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent [x]-> ${event.actionMasked}")
        val handler = gestureDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            if (isScroll) {
                pageChangeIfNeed(if (abs(deltaX) > abs(deltaY)) deltaX else deltaY)
                downX = -1f
                downY = -1f
                deltaX = 0f
                deltaY = 0f
                isScroll = false
            }
        }
        return handler
        //return gestureDetector.onTouchEvent(event)
    }

    /**
     * 根据手势拍段是否切页
     */
    private fun pageChangeIfNeed(delta: Float) {
        Log.d(TAG, "pageChangeIfNeed -> delta: $delta")
        if (abs(delta) > pagingTouchSlop) {
            if (delta > 0) previous()
            else next()
        }
    }

    private fun reloadUiWithAdapterInit(oldItemsInPage: Int) {
        // 确保当前view的大小已确定
        // TODO: restore page data? updateIndicatorAndInvalidate(oldItemsInPage)
        Log.d(TAG, "reloadUiWithAdapterInit() invoke")
        post {
            reloadUiWithConfigurationChange(oldItemsInPage, true)
        }
    }

    // 不需要重新加载数据，数据没有变化，但是样式的相关参数变了(rows, columns, viewSize(不考虑)), itemWidth、rect也需要重新计算
    /**
     * @param reSort 是否重新排序布局, if true: currentPage从0开始，展示第一页，否则根据scaleMode进行
     * */
    private fun reloadUiWithConfigurationChange(oldItemsInPage: Int, reSort: Boolean) {
        Log.d(TAG, "reloadUiWithConfigurationChange() invoke -> reSort: $reSort")
        calculateItemProperty()
        calculateRect()
        updateIndicatorAndInvalidate(oldItemsInPage, reSort = reSort)
        //invalidate()
    }

    // 刷新ui,数据变化导致的ui刷新
    private fun reloadUiWithDataChange() {
        Log.d(TAG, "reloadUiWithDataChange() invoke")
        invalidate()
    }

    private fun reloadUiWithDataChange(position: Int) {
        // 如果position在当前页时（可见）才刷新，否则忽略
        val page = adapter!!.getPageWithPosition(position)
        if (currentPage == page) {
            val offset = position % adapter!!.itemsInPage
            invalidate(itemRectCache[offset].toRect())
        } else {
            Log.d(TAG, "reloadUiWithDataChange -> $position is invisible, skip refresh ui")
        }
    }

    /**计算部分**/
    private fun calculateItemProperty() {
        val titleHeight = titlePaint.fontMetrics.let { it.bottom - it.top }
        // 各item相关的宽高数据
        val mWidth = width - paddingStart - paddingEnd
        val mHeight = height - paddingTop - paddingBottom
        // item padding 后续再考虑
        val maxCoverWidth = (mWidth - (columns - 1) * itemMinXGap) / columns
        val maxCoverHeight = (mHeight - (rows - 1) * itemMinYGap) / rows - titleHeight

        val needAdjust = coverWidthAttr > maxCoverWidth || coverHeightAttr > maxCoverHeight
        var aspect = 1.0f
        if (needAdjust) {
            val widthRatio = coverWidthAttr / maxCoverWidth
            val heightRatio = coverHeightAttr / maxCoverHeight
            aspect = widthRatio.coerceAtLeast(heightRatio)
        }
        // cover的size数据
        coverWidth = coverWidthAttr / aspect
        coverHeight = coverHeightAttr / aspect

        // 右上角的图标大小也需同步放缩
        rtSize = rtSizeAttr / aspect

        // item的size数据
        itemWidth = coverWidth
        itemHeight = coverHeight + titleHeight

        // space gap数据
        if (columns > 1) {
            xGap = (mWidth - itemWidth * columns) / (columns - 1)
        }
        if (rows > 1) {
            yGap = (mHeight - itemHeight * rows) / (rows - 1)
        }

        // baseLine数据
        titleBaseline = coverHeight - titlePaint.fontMetrics.top
        rbBaseline = coverHeight - rbTextPaint.fontMetrics.bottom
    }

    // 计算并保存各rect数据，用户后续填充样式数据
    private fun calculateRect() {
        synchronized(this) {
            itemRectCache.clear()
            coverRectCache.clear()
            rtRectCache.clear()
            rbRectCache.clear()
            ltRectCache.clear() // lt待定
        }
        if (rows == INVALID || columns == INVALID) {
            Log.e(TAG, "ensure adapter init [Y]")
            return
        }
        // 这里只是一页的rect，进行复用
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val left = column * (itemWidth + xGap) + paddingStart
                val top = row * (itemHeight + yGap) + paddingTop
                val rtRight = left + itemWidth - rtMarginRightDimension
                val rtTop = top + rtMarginTopDimension + paddingTop
                val rtSize = rtSize
                val rbTop =
                    top + coverHeight - rbTextPaint.fontMetrics.let { it.bottom - it.top }
                itemRectCache.add(RectF(left, top, left + itemWidth, top + itemHeight))
                coverRectCache.add(RectF(left, top, left + itemWidth, top + coverHeight))
                rtRectCache.add(RectF(rtRight - rtSize, rtTop, rtRight, rtTop + rtSize))
                // 先预留整个width长度
                rbRectCache.add(RectF(left, rbTop, left + itemWidth, top + coverHeight))
            }
        }
    }

    private fun getRealPosition(positionInPage: Int): Int {
        return adapter?.run {
            currentPage * itemsInPage + positionInPage
        } ?: throw IllegalStateException("please ensure the FlexibleGridView adapter init [X]")
    }

    val pagingTouchSlop = 16f
    var downX = -1f
    var downY = -1f
    var deltaX = 0f
    var deltaY = 0f
    var isScroll = false

    // 手势识别，用来检测相关操作及回调
    val gestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent): Boolean {
                downX = e.x
                downY = e.y
                return true
            }

            override fun onShowPress(e: MotionEvent) {}

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (isScroll) return false
                itemRectCache.forEachIndexed { positionInPage, rect ->
                    val realPosition = getRealPosition(positionInPage)
                    if (rect.contains(e.x, e.y) && realPosition < adapter!!.totalSize) {
                        adapter?.clickCallback?.onSingleTapUp(realPosition)
                        return true
                    }
                }
                return false
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float,
            ): Boolean {
                if (downX < 0) {
                    downX = e1?.x ?: e2.x
                    downY = e1?.y ?: e2.y
                }
                deltaX = e2.x - downX
                deltaY = e2.y - downY
                if (abs(deltaX).coerceAtLeast(abs(deltaY)) >= pagingTouchSlop) {
                    isScroll = true
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                if (isScroll) return
                itemRectCache.forEachIndexed { positionInPage, rect ->
                    val realPosition = getRealPosition(positionInPage)
                    if (rect.contains(e.x, e.y) && realPosition < adapter!!.totalSize) {
                        adapter?.clickCallback?.onLongPress(realPosition)
                        return
                    }
                }
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float,
            ): Boolean {
                return false
            }
        })

    private val pageChangeListeners by lazy { arrayListOf<PagerView.OnPageChangeListener>() }

    // rows / columns的改动都改这
    var adapter: Adapter<*>? = null    // 如果adapter为null时，默认作为还没有数据
        set(value) {
            val oldItemsInPage = field?.itemsInPage ?: 0
            field?.unRegisterObserver(mObserver)
            field = value
            if (value == null) {
                throw IllegalArgumentException("setAdapter() is null")
            } else {
                value.registerObserver(mObserver)
                reloadUiWithAdapterInit(oldItemsInPage)
                // 相关翻页信息也需更新
            }
        }
    private var rows: Int = INVALID
        get() {
            return if (adapter == null) {
                Log.e(TAG, "adapter need set at first when get [rows]")
                INVALID
            } else {
                adapter!!.rows
            }
        }
    private var columns: Int = INVALID
        get() {
            return if (adapter == null) {
                Log.e(TAG, "adapter need set at first when get [columns]")
                INVALID
            } else {
                adapter!!.columns
            }
        }

    // 保持currentPage
    override var currentPage: Int = 0
        set(value) {
            val old = field
            //val expect = if (value >= pageCount) pageCount - 1 else value
            field = value
            // 页面数据修改
            adapter?.let {
                reloadUiWithDataChange()
                notifyPageChange(field, old)
            }
        }

    override var isLooper = true

    override val pageCount: Int
        get() {
            return adapter?.pageCount ?: 0
        }

    override fun previous(): Boolean {
        var current = currentPage
        if (current == 0 && !isLooper) {
            notifyPageChangeError(PagerView.ERROR_ISFIRST)
            return false
        }
        current--
        if (current < 0) {
            current = 0.coerceAtLeast(pageCount - 1)
        }
        currentPage = current
        return true
    }

    override fun next(): Boolean {
        var current = currentPage
        if (current == pageCount - 1 && !isLooper) {
            notifyPageChangeError(PagerView.ERROR_ISLAST)
            return false
        }
        current++
        if (current >= pageCount) {
            current = 0
        }
        currentPage = current
        return true
    }

    /**
     * page数据需要变动时
     * @param reSort 是否重新排序布局, if true: currentPage从0开始，展示第一页，否则根据scaleMode进行
     * */
    private fun updateIndicatorAndInvalidate(oldItemsInPage: Int, reSort: Boolean = true) {
        notifyPageCountChanged(pageCount)
        if (reSort) {
            currentPage = 0
            return
        }
        // currentPage设置会触发onDraw绘制，这里存在部分的耦合
        val oldPage = currentPage
        currentPage = when (scaleMode) {
            ScaleMode.ObeyPosition -> {
                //adapter!!.getPageWithPosition(currentPage * oldItemsInPage)
                // 存在新设置的adapter数据集变化，会使items position/ totalSize变化,防止越界
                val position = oldPage * oldItemsInPage
                adapter!!.getPageWithPosition(position.coerceAtMost(adapter!!.totalSize - 1))
            }

            ScaleMode.ObeyPage -> {
                oldPage
            }

            else -> {
                throw UnsupportedOperationException("ScaleMode unknown type")
            }
        }
    }

    override fun addOnPageChangeListener(listener: PagerView.OnPageChangeListener) {
        synchronized(pageChangeListeners) {
            if (pageChangeListeners.contains(listener)) return
            pageChangeListeners.add(listener)
        }
    }

    override fun removeOnPageChangeListener(listener: PagerView.OnPageChangeListener) {
        synchronized(pageChangeListeners) {
            pageChangeListeners.remove(listener)
        }
    }

    private fun notifyPageChangeError(errorCode: Int) {
        synchronized(pageChangeListeners) {
            pageChangeListeners.forEach {
                it.onPageChangeError(this, errorCode)
            }
        }
    }

    private fun notifyPageChange(current: Int, previous: Int) {
        synchronized(pageChangeListeners) {
            pageChangeListeners.forEach {
                it.onPageChange(this, current, previous)
            }
        }
    }

    // page总数变更
    private fun notifyPageCountChanged(pageCount: Int) {
        synchronized(pageChangeListeners) {
            pageChangeListeners.forEach {
                it.onPageCountChange(this, pageCount)
            }
        }
    }

    // 建立一个adapter进行对数据的管理以及业务逻辑的控制
    abstract class Adapter<T>(
        val data: MutableList<T>,
        rows: Int,
        columns: Int,
        val clickCallback: IClickCallback? = null,
    ) : IDataStation, ListPager {
        private val observable = FlexibleGridObservable()

        var rows: Int = rows
            private set(value) {
                field = value
            }
        var columns: Int = columns
            private set(value) {
                field = value
            }

        val pageCount: Int
            get() {
                return totalSize / itemsInPage + if (totalSize % itemsInPage == 0) 0 else 1
            }

        override val itemsInPage: Int
            get() {
                return rows * columns
            }

        val totalSize: Int
            get() {
                return data.size
            }

        /**
         * @param page from 0 to pageCount - 1
         * */
        override fun getFirstPositionInPage(page: Int): Int {
            if (page < 0 || page >= pageCount) {
                throw Utils.indexError("Position", page, pageCount)
            }
            return page * itemsInPage
        }

        /**
         * @param position the real position in data, from 0 to totalSize
         * */
        override fun getPageWithPosition(position: Int): Int {
            if (totalSize == 0) return 0
            if (position < 0 || position >= totalSize) {
                throw Utils.indexError("Position", position, totalSize)
            }
            return position / itemsInPage
        }

        fun getCurrentPageData(currentPage: Int): List<T> {
            Log.d(
                TAG,
                "getCurrentPageData currentPage:$currentPage"
            )
            val from = currentPage * itemsInPage
            val nextPageFirst = (currentPage + 1) * itemsInPage
            val to = if (nextPageFirst <= totalSize) nextPageFirst else totalSize
            Log.d(TAG, "getCurrentPageData : from:$from -> to:$to")
            return mutableListOf<T>().apply {
                for (index in from until to) {
                    add(data[index])
                }
            }
        }

        /**
         * todo 后续可优化差量更新
         * */
        fun submitData(list: List<T>) {
            data.clear()
            data.addAll(list)
        }

        /**预留拓展相关数据变化时得回调**/
        fun notifyDataSetChange() {
            observable.notifyDataChange()
        }

        /**
         * 修改行/列值，触发ui更新
         * */
        fun notifyConfigurationChange(rows: Int, columns: Int) {
            val oldItemsInPage = this.rows * this.columns
            this.rows = rows
            this.columns = columns
            observable.notifyConfigurationChange(oldItemsInPage)
        }

        /**
         * 局部绘制
         * */
        fun notifyPositionDataChange(position: Int, item: T) {
            data[position] = item
            observable.notifyPositionDataChange(position)
        }

        fun registerObserver(observer: FlexibleGridDataSetObserver) {
            observable.registerObserver(observer)
        }

        fun unRegisterObserver(observer: FlexibleGridDataSetObserver) {
            observable.unregisterObserver(observer)
        }
    }


    private class FlexibleGridObservable : Observable<FlexibleGridDataSetObserver>() {
        fun notifyDataChange() {
            mObservers.forEach {
                it.onChanged()
            }
        }

        fun notifyPositionDataChange(position: Int) {
            mObservers.forEach {
                it.onPositionChanged(position)
            }
        }

        fun notifyConfigurationChange(oldItemsInPage: Int) {
            mObservers.forEach {
                it.onConfigurationChange(oldItemsInPage)
            }
        }
    }

    /**
     * 数据观察者接口
     * */
    abstract class FlexibleGridDataSetObserver {
        abstract fun onChanged()

        /**
         * @param position 真实的position，并非当前页偏移
         * */
        abstract fun onPositionChanged(position: Int)

        /**
         * 行列数据变化后相关
         * @param oldItemsInPage 变化前的一页容量
         * */
        abstract fun onConfigurationChange(oldItemsInPage: Int)
    }

    /**
     * 点击相关
     * */
    interface IClickCallback {
        /**
         * @param position real position in data set, from 0 to totalSize
         * */
        fun onSingleTapUp(position: Int)

        fun onLongPress(position: Int)
    }

    interface IDataStation {
        /**
         * @param position 真实的位置，非当页position
         * */
        fun getCoverBitmap(position: Int, expectWidth: Float, expectHeight: Float): Bitmap

        fun getTitleText(position: Int): String

        fun getProgressDescriptions(position: Int): String

        fun getRtConfig(position: Int): FlexibleRtConfig
    }

    /**
     * @param need 是否需要显示
     * @param bitmap 如果需要显示时，此bitmap不为空，并显示
     * @param ext 需要显示的额外信息，备用
     * */
    data class FlexibleRtConfig(
        val need: Boolean = false,
        val src: Bitmap? = null,
        val ext: String? = null,
    )

    /**
     * 当尺寸变化时（rows/columns）,变化后的数据显示根据何种模式来显示
     * ObeyPosition: 根据变化前的第一个元素，变化后依然是第一个元素
     * ObeyPage: 根据变化前的当前页数，变化后依然是此页数，除非超过最大页数
     * */
    sealed class ScaleMode {
        object ObeyPosition : ScaleMode()
        object ObeyPage : ScaleMode()
    }
}