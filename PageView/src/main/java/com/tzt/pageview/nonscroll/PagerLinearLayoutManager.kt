package com.tzt.pageview.nonscroll

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.tzt.pageview.R
import androidx.core.content.withStyledAttributes

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 15:57
 */
/**
 * 分页列表布局管理
 */
class PagerLinearLayoutManager : LinearLayoutManager, PagerModel {

    companion object {
        private const val DEFAULT_ROW_COUNT = 8
    }

    private var itemSize: Int = 0
    override val itemsInPage: Int
        get() = rowCount
    override val columns: Int = 1
    private var rowCount =
        DEFAULT_ROW_COUNT
        set(value) {
            val tmp = if (value < 0) DEFAULT_ROW_COUNT else value
            if (field == tmp) return
            field = tmp
        }

    @JvmOverloads
    constructor(
        context: Context, rowCount: Int = DEFAULT_ROW_COUNT,
        orientation: Int = RecyclerView.VERTICAL, reverseLayout: Boolean = false,
    ) : super(context,
        orientation, reverseLayout) {
        this.rowCount = rowCount
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes) {
        context.withStyledAttributes(attrs, R.styleable.PageGridStyleable) {
            rowCount = getInt(
                R.styleable.PageGridStyleable_rowCount,
                DEFAULT_ROW_COUNT
            )
        }
    }

    /*override fun getFirstPositionInPage(page: Int): Int {
        val total = itemCount
        if (total == 0) return 0

        val pageCount = total / itemsInPage + if (total % itemsInPage == 0) 0 else 1
        if (page < 0 || page >= pageCount) {
            throw Utils.indexError("Page", page, pageCount)
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

    // 如果是横向，重新算宽度
    override fun getDecoratedMeasuredWidth(child: View): Int {
        if (orientation == HORIZONTAL) {
            return itemSize
        }
        return super.getDecoratedMeasuredWidth(child)
    }

    // 如果是纵向，重新算高度
    override fun getDecoratedMeasuredHeight(child: View): Int {
        if (orientation == VERTICAL) {
            return itemSize
        }
        return super.getDecoratedMeasuredHeight(child)
    }

    override fun canScrollHorizontally(): Boolean = false

    override fun canScrollVertically(): Boolean = false

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
            if(orientation == VERTICAL) {
                itemSize = (View.MeasureSpec.getSize(heightSpec) - paddingTop - paddingBottom) / rowCount
                //算出来后重新设置一下，因为宽高可能会除不尽，导致抖动
                super.onMeasure(
                    recycler,
                    state,
                    widthSpec,
                    View.MeasureSpec.makeMeasureSpec(itemSize * rowCount, View.MeasureSpec.EXACTLY)
                )
            }else {
                itemSize = (View.MeasureSpec.getSize(widthSpec) - paddingStart - paddingEnd) / rowCount
                //算出来后重新设置一下，因为宽高可能会除不尽，导致抖动
                super.onMeasure(
                    recycler,
                    state,
                    View.MeasureSpec.makeMeasureSpec(itemSize * rowCount, View.MeasureSpec.EXACTLY),
                    heightSpec
                )
            }
        } else {
            super.onMeasure(recycler, state, widthSpec, heightSpec)
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        if(itemSize == 0) {
            return
        }
        super.onLayoutChildren(recycler, state)
    }
}