package com.tzt.pageview.nonscroll

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tzt.pageview.R
import androidx.core.content.withStyledAttributes

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 15:59
 */
/**
 * 分页网格布局管理
 */
class PagerGridLayoutManager : LinearLayoutManager, PagerModel {

    companion object {
        private const val DEFAULT_ROW_COUNT = 4
        private const val DEFAULT_COLUMN_COUNT = 4
    }

    private var itemWidth: Int = 0
    private var itemHeight: Int = 0

    override val itemsInPage: Int
        get() = rows * _columns

    override val columns: Int
        get() = _columns

    private var rows =
        DEFAULT_ROW_COUNT
        set(value) {
            val tmp = if (value < 0) DEFAULT_ROW_COUNT else value
            if (field == tmp) return
            field = tmp
        }
    private var _columns =
        DEFAULT_COLUMN_COUNT
        set(value) {
            val tmp = if (value < 0) DEFAULT_COLUMN_COUNT else value
            if (field == tmp) return
            field = tmp
        }

    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        context.withStyledAttributes(
            attrs,
            R.styleable.PageGridStyleable,
            defStyleAttr, defStyleRes
        ) {
            rows = getInt(
                R.styleable.PageGridStyleable_rowCount,
                DEFAULT_ROW_COUNT
            )
            _columns = getInt(R.styleable.PageGridStyleable_columnCount, 2)
        }
    }

    /**
     * @param rowCount 是gridView中的行列数据，而非recyclerView
     * @param columnCount 是gridView中的行列数据，而非recyclerView
     * */
    @JvmOverloads
    constructor(
        context: Context,
        rowCount: Int,
        columnCount: Int,
        orientation: Int = RecyclerView.VERTICAL, reverseLayout: Boolean = false,
    ) : super(
        context,
        orientation,
        reverseLayout
    ) {
        this.rows = rowCount
        this._columns = columnCount
    }

    /*override fun getFirstPositionInPage(page: Int): Int {
        val total = itemCount
        if (total == 0) return 0

        val pageCount = total / itemsInPage + if (total % itemsInPage == 0) 0 else 1
        if (page < 0 || page >= pageCount) {
            throw Utils.indexError("Position", page, pageCount)
        }
        return page * itemsInPage
    }

    override fun getPageWithPosition(position: Int): Int {
        val total = itemCount
        if (total == 0) return 0

        if (position < 0 || position >= total) {
            throw Utils.indexError("Position", position, total)
        }
        return position / itemsInPage
    }*/

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int,
    ) {
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val width = View.MeasureSpec.getSize(widthSpec)
        val height = View.MeasureSpec.getSize(heightSpec)
        if (widthMode == View.MeasureSpec.EXACTLY && heightMode == View.MeasureSpec.EXACTLY && width * height != 0) {
            itemWidth = width / 1
            itemHeight = height / 1 //
            //算出来后重新设置一下，因为宽高可能会除不尽，导致抖动
            super.onMeasure(
                recycler,
                state,
                View.MeasureSpec.makeMeasureSpec(itemWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(itemHeight, View.MeasureSpec.EXACTLY)
            )
        } else {
            super.onMeasure(recycler, state, widthSpec, heightSpec)
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        if (itemWidth * itemHeight == 0) {
            return
        }
        super.onLayoutChildren(recycler, state)
    }

    override fun getDecoratedMeasuredHeight(child: View): Int = itemHeight

    override fun getDecoratedMeasuredWidth(child: View): Int = itemWidth
}