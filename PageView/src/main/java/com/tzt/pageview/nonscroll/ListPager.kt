package com.tzt.pageview.nonscroll

/**
 * 列表控件分页接口
 */
interface ListPager {
    val itemsInPage: Int

    /**
     * 获取对应页码的第一项所在位置，用于跳转到该位置实现切页功能
     * @param page 对应页码
     * @return 对应页码的第一项所在位置
     */
    fun getFirstPositionInPage(page: Int): Int

    /**
     * 获取对应位置所在页码
     * @param position 对应位置
     * @return 对应位置所在页码
     */
    fun getPageWithPosition(position: Int): Int
}