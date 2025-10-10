package com.tzt.pageview.nonscroll

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.annotation.CallSuper
import com.tzt.pageview.R

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 16:28
 */
/**
 * 页码指示器基类
 */
abstract class BasePagerIndicator @JvmOverloads constructor(context: Context,
                                                            attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr),
    PagerIndicator {

    protected var pagerView: PagerView? = null
    private lateinit var previous:ImageView
    private lateinit var next:ImageView
    /**
     * 是否显示切页按钮
     */
    var isShowIndicatorButton = true
        set(value) {
            field = value
            previous.visibility = if (value) VISIBLE else INVISIBLE
            next.visibility = if (value) VISIBLE else INVISIBLE
        }
    /**
     * 切页按钮与指示器内容间距
     */
    var indicatorButtonPadding = 0
        set(value) {
            field = value

//            val lpPrevious = generateIndicatorButtonLayoutParams(previous.layoutParams)
//            lpPrevious.marginEnd = value
//            previous.layoutParams = lpPrevious
//            val lpNext = generateIndicatorButtonLayoutParams(next.layoutParams)
//            lpNext.marginStart = value
//            next.layoutParams = lpNext
        }

    /**
     * 向前切页按钮图标
     */
    var previousDrawable: Drawable
        set(value) {
            previous.setImageDrawable(value)
        }
        get() = previous.drawable

    /**
     * 向后切页按钮图标
     */
    var nextDrawable: Drawable
        set(value) {
            next.setImageDrawable(value)
        }
        get() = next.drawable
    /**
     * 单页是否显示指示器
     */
    var isShowWhenOnePage: Boolean = false
        set(value) {
            field = value
            val pageCount = pagerView?.pageCount ?: 0
            if (pageCount == 0) return

            visibility = if (value) VISIBLE else if (pageCount == 1) INVISIBLE else VISIBLE
        }

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.inkpagerwidget_layout_base_indicator, this)
        previous=findViewById(R.id.inkpagerwidget_action_previous)
        next=findViewById(R.id.inkpagerwidget_action_next)
        val a = context.obtainStyledAttributes(attrs, R.styleable.ZYInkBasePagerIndicator)
        isShowIndicatorButton = a.getBoolean(R.styleable.ZYInkBasePagerIndicator_showIndicatorButton, true)
        indicatorButtonPadding =
            a.getDimensionPixelSize(R.styleable.ZYInkBasePagerIndicator_indicatorButtonPadding, 0)
        isShowWhenOnePage = a.getBoolean(R.styleable.ZYInkBasePagerIndicator_showWhenOnePage, false)
        val previousRes = a.getResourceId(R.styleable.ZYInkBasePagerIndicator_previousDrawable, android.R.drawable.ic_media_previous)
        val nextRes = a.getResourceId(R.styleable.ZYInkBasePagerIndicator_nextDrawable, android.R.drawable.ic_media_next)
        a.recycle()

        setPreviousResource(previousRes)
        setNextResource(nextRes)

        previous.setOnClickListener { pagerView?.previous() }
        next.setOnClickListener { pagerView?.next() }
    }

    private fun generateIndicatorButtonLayoutParams(
        lp: ViewGroup.LayoutParams? = null): LayoutParams = when (lp) {
        null -> LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        is LayoutParams -> lp
        else -> LayoutParams(lp)
    }

    /**
     * 设置向前切页按钮图标资源
     * @param resId 资源id'
     */
    fun setPreviousResource(resId: Int) = previous.setImageResource(resId)

    /**
     * 设置向后切页按钮图标资源
     * @param resId 资源id
     */
    fun setNextResource(resId: Int) = next.setImageResource(resId)

    /**
     * 根据控件配置页码指示器
     * 如果是[PagerRecyclerView]，则必须在调用[PagerRecyclerView.setLayoutManager]后调用，否则会出现异常
     */
    override fun setupWithPagerView(pagerView: PagerView) {
        pagerView.addOnPageChangeListener(this)
        this.pagerView = pagerView
        setPageCount(pagerView.pageCount)
        setCurrentPage(pagerView.currentPage)
        onAttachToPagerView(pagerView)
    }

    override fun onPageChange(pagerView: PagerView, current: Int, previous: Int) {
        setCurrentPage(current)
    }

    override fun onPageChangeError(pagerView: PagerView, errorCode: Int) {}

    override fun onPageCountChange(pagerView: PagerView, newPageCount: Int) {
        setPageCount(newPageCount)
        // currentPage和pageCount均为0，pageCount修改后需要刷新当前页显示
        val current = pagerView.currentPage
        if (current == 0) {
            setCurrentPage(current)
        }
    }

    /**
     * 与控件关联回调函数，可在此做初始化操作
     */
    protected open fun onAttachToPagerView(pagerView: PagerView) {}

    /**
     * 设置总页数
     */
    @CallSuper
    protected open fun setPageCount(pageCount: Int) {
        visibility = when(pageCount) {
            0 -> INVISIBLE
            1 -> if (isShowWhenOnePage) VISIBLE else INVISIBLE
            else -> VISIBLE
        }
    }

    /**
     * 设置当前页
     */
    protected open fun setCurrentPage(current: Int) {}

    fun finalize() {
        pagerView?.removeOnPageChangeListener(this)
    }
}