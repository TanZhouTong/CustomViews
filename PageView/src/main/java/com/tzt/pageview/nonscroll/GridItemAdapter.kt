package com.tzt.pageview.nonscroll

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.BaseAdapter
import android.widget.GridView
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import kotlin.math.roundToInt

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 13:46
 */
class GridItemAdapter<T>(
    val context: Context,
    val gridItemViewBind: IGridItemViewBind<T>,
    val columns: Int,
    val rows: Int,
) : BaseAdapter() {

    companion object {
        const val TAG = "GridItemAdapter"
    }

    protected val data: MutableList<T> = mutableListOf()
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): T {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val view = gridItemViewBind.viewBind(
            context = context,
            data = data,
            position = position,
            convertView = convertView,
            parent = parent
        )
        val maxWidth = /*context.resources.getDimensionPixelSize(R.dimen.)*/200
        // 这里对view判断
        val lp = view.layoutParams
        val height = lp.height
        val width = lp.width
        val desiredAspect = width.toFloat() / height
        parent as GridView
        val pWidth = parent.width
        val remainWidth = pWidth - ((view.marginStart + view.marginEnd + parent.paddingStart + parent.paddingEnd) + (columns - 1) * parent.requestedHorizontalSpacing)
        var remainMaxWidth = remainWidth / columns

        val pHeight = parent.height
        val remainHeight = pHeight - ((view.marginTop + view.marginBottom + parent.paddingTop + parent.paddingBottom) + (rows - 1) * parent.verticalSpacing)
        var remainMaxHeight = remainHeight / rows
        Log.d(TAG, "pWidth: $pWidth, remainMaxWidth: $remainMaxWidth")
        lp.width = width.coerceAtMost(maxWidth.coerceAtMost(remainMaxWidth))
        lp.height = (lp.width / desiredAspect).roundToInt()
        Log.d(TAG, "1 width: ${lp.width}, height: ${lp.height}")

        lp.height = lp.height.coerceAtMost(remainMaxHeight)
        lp.width = (lp.height * desiredAspect).roundToInt()
        Log.d(TAG, "2 width: ${lp.width}, height: ${lp.height}")
        parent.post {
            parent.columnWidth = lp.width
        }
        return view
    }

    fun submitData(data: List<*>) {
        Log.d(TAG, "submitData size:${data.size}-> data:${data[0]}")
        this.data.clear()
        this.data.addAll(data as List<T>)
    }

    interface IGridItemViewBind<T1> {
        fun viewBind(
            context: Context,
            data: List<T1>,
            position: Int,
            convertView: View?,
            parent: ViewGroup,
        ): View
    }
}