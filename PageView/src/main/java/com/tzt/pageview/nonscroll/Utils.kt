package com.tzt.pageview.nonscroll

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 10:43
 */
internal object Utils {
    internal fun layoutManagerError() =
        IllegalArgumentException("LayoutManager must be implements by ListPager")

    internal fun indexError(name: String, errorIndex: Int, total: Int) =
        IllegalAccessError("$name out of Bounds: $errorIndex, total: $total")
}