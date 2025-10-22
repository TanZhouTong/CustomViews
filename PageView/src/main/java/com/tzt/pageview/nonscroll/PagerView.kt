package com.tzt.pageview.nonscroll

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 10:15
 */
interface PagerView {
    companion object {
        // 错误码
        /**
         * 未知错误
         */
        const val ERROR_UNKNOWN = 0
        /**
         * 错误：已经是第一页
         */
        const val ERROR_ISFIRST = -1
        /**
         * 错误：已经是最后一页
         */
        const val ERROR_ISLAST = 1
    }
    /**
     * 当前页
     * from 0 to (pageCount - 1)
     * */
    val currentPage: Int

    /**
     * 总页数
     * */
    val pageCount: Int

    /**
     * 是否循环
     * */
    var isLooper: Boolean

    /**
     * 前一页
     * @return 切换成功返回true，否则false
     */
    fun previous(): Boolean

    /**
     * 后一页
     * @return 切换成功返回true，否则false
     */
    fun next(): Boolean

    /**
     * 指定页
     * @param page :from 1 to pageCount
     * @return 切换成功返回true，否则false
     * */
    fun to(page: Int): Boolean

    /**
     * 添加页码改变监听器
     */
    fun addOnPageChangeListener(listener: OnPageChangeListener)

    /**
     * 移除页码改变监听器
     */
    fun removeOnPageChangeListener(listener: OnPageChangeListener)

    /**
     * 页码改变监听器接口
     */
    interface OnPageChangeListener {
        /**
         * 当前页改变接口
         * @param pagerView 当前控件
         * @param current 当前页
         * @param previous 之前的当前页
         */
        fun onPageChange(pagerView: PagerView, current: Int, previous: Int)

        /**
         * 切页错误接口
         * @param pagerView 当前控件
         * @param errorCode 错误码
         */
        fun onPageChangeError(pagerView: PagerView, errorCode: Int)

        /**
         * 总页数改变接口
         * @param pagerView 当前控件
         * @param newPageCount 新的总页数
         */
        fun onPageCountChange(pagerView: PagerView, newPageCount: Int)
    }
}