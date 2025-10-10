package com.tzt.pageview.nonscroll

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.tzt.pageview.R

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 16:26
 */
/**
 * 文本页码指示器
 */
class TextPagerIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                   defStyleAttr: Int = 0) : BasePagerIndicator(context, attrs, defStyleAttr) {

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ZYInkTextPagerIndicator)
        setIndicatorLayout(a.getResourceId(R.styleable.ZYInkTextPagerIndicator_indicatorLayout,
            R.layout.inkpagerwidget_layout_default_number_indicator))
        a.recycle()
    }

    /**
     * 设置页码指示器内容自定义布局资源
     * 必须设置包含id为[R.id.inkpagerview_tv_current]和[R.id.inkpagerview_tv_total]的[TextView]的布局
     */
    fun setIndicatorLayout(layoutRes: Int) {
        val inflater = LayoutInflater.from(context)
        val content = findViewById<FrameLayout>(R.id.inkpagerwidget_indicator_content)
        val view = inflater.inflate(layoutRes, content, false)
        content.removeAllViews()
        content.addView(view)
    }

    override fun setCurrentPage(current: Int) {
        val total = pagerView?.pageCount ?: 0
        val content = findViewById<FrameLayout>(R.id.inkpagerwidget_indicator_content)
        val currentView = content.findViewById<TextView>(
            R.id.inkpagerview_tv_current)
        if (total == 0 && current == 0) {
            currentView.text = current.toString()
            return
        }
        if (current < 0 || current >= total) return

        currentView.text = (current + 1).toString()
    }

    override fun setPageCount(pageCount: Int) {
        super.setPageCount(pageCount)
        if (pageCount < 0) return
        val content = findViewById<FrameLayout>(R.id.inkpagerwidget_indicator_content)
        content.findViewById<TextView>(R.id.inkpagerview_tv_total).text = pageCount.toString()
    }
}