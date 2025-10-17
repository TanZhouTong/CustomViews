package com.tzt.pageview.gridview

import android.annotation.SuppressLint
import android.content.Context
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
import com.tzt.pageview.nonscroll.WrapperGridAdapter
import androidx.core.graphics.withSave
import kotlin.math.roundToInt

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/16 13:53
 */
class FlexibleGridView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val INVALID = -1
        const val TAG = "FlexibleGridView"
    }

    val itemRectCache: MutableList<RectF> = mutableListOf()     // 各子item的rect
    val coverRectCache: MutableList<RectF> = mutableListOf()    // 子item cover背景的rect
    val rtRectCache: MutableList<RectF> = mutableListOf()       // 子item 右上角预留的rect，绘制bitmap
    val rbRectCache: MutableList<RectF> = mutableListOf()       // 子item 右下角rect，用户文件状态描述
    val ltRectCache: MutableList<RectF> = mutableListOf()       // 子item 左上角预留的rect，内容待定

    var adapter: Adapter? = null    // 如果adapter为null时，默认作为还没有数据
        set(value) {
            field = value
            if (value == null) {
                throw IllegalArgumentException("setAdapter() is null")
            } else {
                reloadUiWithAdapterInit()
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

    // attrs
    var coverWidthAttr: Float = 0f
    var coverHeightAttr: Float = 0f
    var itemMinXGap: Float = 0f
    var itemMinYGap: Float = 0f
    var isCircle: Boolean = true
    var itemCornerDimension: Float = 0f
    var rtMarginTopDimension: Float = 0f
    var rtMarginRightDimension: Float = 0f
    var rbTextSizeDimension: Float = 0f
    var rbMarginVerticalDimension: Float = 0f
    var rbMarginHorizontalDimension: Float = 0f
    var titleTextSizeDimension: Float = 0f

    // property
    var itemWidth = 0f
    var itemHeight = 0f
    var coverWidth = 0f
    var coverHeight = 0f

    var xGap = 0f
    var yGap = 0f

    // text 基线，相对于item的distance
    var rbBaseline = 0f
    var titleBaseline = 0f

    // tool
    lateinit var strokePaint: Paint
    lateinit var rbPaint: Paint
    lateinit var titlePaint: Paint
    lateinit var bitmapPaint: Paint

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
            isCircle = getBoolean(R.styleable.FlexibleGridView_circle, true)
            itemCornerDimension = getDimension(
                R.styleable.FlexibleGridView_corner,
                context.resources.getDimension(R.dimen.flexible_grid_view_default_corner_size)
            )
            // rt
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
            titleTextSizeDimension = getDimension(
                R.styleable.FlexibleGridView_titleTextSize,
                context.resources.getDimension(R.dimen.flexible_grid_view_default_title_text_size)
            )
        }

        // paint
        strokePaint = Paint().apply {
            strokeWidth = 5f
            color = Color.BLACK
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        rbPaint = Paint().apply {
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
            color = Color.BLACK
            textSize = rbTextSizeDimension
        }
        titlePaint = Paint().apply {
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
            color = Color.GRAY
            textSize = titleTextSizeDimension
        }

        bitmapPaint = Paint().apply {
            color = Color.WHITE
//            style = Paint.Style.FILL
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
        if (adapter == null || adapter!!.rows <= 0 || adapter!!.columns <= 0) {
            Log.w(
                TAG,
                "onSizeChanged() error with -> adapter: $adapter, rows: $rows, columns: $columns"
            )
            return
        }
        // 计算rect
        //reloadUiWithConfigurationChange()
    }

    lateinit var canvasCache: Canvas
    lateinit var cacheBitmap: Bitmap

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
            adapter?.currentPageData?.forEachIndexed { index, item ->
                drawGridItem(index, item)
            }
            canvas.drawBitmap(cacheBitmap, 0f, 0f, bitmapPaint)
        }
    }

    private fun drawGridItem(index: Int, item: T) {

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
                createBitmap(
                    (coverWidth + 0.5f).roundToInt(),
                    (coverHeight + 0.5f).roundToInt(),
                    Bitmap.Config.ARGB_8888
                ),
                null,
                coverRect,
                bitmapPaint
            )
        }

        // 2. draw 外框
        canvasCache.drawRoundRect(
            coverRectCache[index],
            itemCornerDimension,
            itemCornerDimension,
            strokePaint
        )

        // 3. 画右上角select

        // 4. 画rb下载状态

        // 5. 画title字体
        val itemRect = itemRectCache[index]
        canvasCache.drawText(
            "test[${adapter!!.currentPage}]-[$index]",
            itemRect.left,
            itemRect.top + titleBaseline,
            titlePaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    // 手势识别，用来检测相关操作及回调
    val gestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onShowPress(e: MotionEvent) {}

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                performClick()
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float,
            ): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {

            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float,
            ): Boolean {
                return true
            }
        })

    private fun reloadUiWithAdapterInit() {
        // 确保当前view的大小已确定
        Log.d(TAG, "reloadUiWithAdapterInit() invoke")
        post {
            reloadUiWithConfigurationChange()
        }
    }

    // 不需要重新加载数据，数据没有变化，但是样式的相关参数变了(rows, columns, viewSize(不考虑)), itemWidth、rect也需要重新计算
    private fun reloadUiWithConfigurationChange() {
        Log.d(TAG, "reloadUiWithConfigurationChange() invoke")
        calculateItemProperty()
        calculateRect()
        invalidate()
    }

    // 刷新ui,数据变化导致的ui刷新
    private fun reloadUiWithDataChange() {
        Log.d(TAG, "reloadUiWithDataChange() invoke")
        invalidate()
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
        rbBaseline = coverHeight - rbPaint.fontMetrics.bottom
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
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val left = column * (itemWidth + xGap) + paddingStart
                val top = row * (itemHeight + yGap) + paddingTop
                val rtRight = left + itemWidth - rtMarginRightDimension - paddingEnd
                val rtTop = top + rtMarginTopDimension + paddingTop
                val rtSize =
                    context.resources.getDimensionPixelSize(R.dimen.flexible_grid_view_default_rt_size)
                val rbTop = top + coverHeight - rbPaint.fontMetrics.let { it.bottom - it.top }
                itemRectCache.add(RectF(left, top, left + itemWidth, top + itemHeight))
                coverRectCache.add(RectF(left, top, left + itemWidth, top + coverHeight))
                rtRectCache.add(RectF(rtRight - rtSize, rtTop, rtRight, rtTop + rtSize))
                // 先预留整个width长度
                rbRectCache.add(RectF(left, rbTop, left + itemWidth, top + coverHeight))
            }
        }
    }

    // 建立一个adapter进行对数据的管理以及业务逻辑的控制
    inner class Adapter(private val data: MutableList<T>, var rows: Int, var columns: Int) {

        var currentPage: Int = 0

        val currentPageData: List<T>
            get() {
                Log.d(
                    WrapperGridAdapter.Companion.TAG,
                    "getCurrentPageData currentPage:$currentPage"
                )
                val from = currentPage * itemsInPage
                val nextPageFirst = (currentPage + 1) * itemsInPage
                val to = if (nextPageFirst <= totalSize) nextPageFirst else totalSize
                Log.d(TAG, "getCurrentPageData : from:$from -> to:$to")
                return data.subList(from, to)
            }

        val itemsInPage: Int
            get() {
                return rows * columns
            }

        val totalSize: Int
            get() {
                return data.size
            }

        val pageCount: Int
            get() {
                return totalSize / itemsInPage + if (totalSize % itemsInPage == 0) 0 else 1
            }

        fun submitData(list: List<T>) {
            data.clear()
            data.addAll(list)
        }

        fun notifyDataSetChange() {
            reloadUiWithDataChange()
        }

        // todo page相关的管理
    }
}