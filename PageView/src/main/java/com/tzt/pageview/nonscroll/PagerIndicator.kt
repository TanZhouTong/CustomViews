package com.tzt.pageview.nonscroll

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 16:27
 */
/**
 * 页码指示器接口
 */
interface PagerIndicator: PagerView.OnPageChangeListener {
    /**
     * 根据控件配置页码指示器
     */
    fun setupWithPagerView(pagerView: PagerView)
}