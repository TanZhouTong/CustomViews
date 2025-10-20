package com.tzt.customviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tzt.pageview.gridview.FlexibleGridView

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/17 15:15
 */
class FlexibleAdapter(
    private val context: Context,
    data: MutableList<FlexibleItem>,
    rows: Int,
    columns: Int,
    clickCallback: FlexibleGridView.IClickCallback? = null
) : FlexibleGridView.Adapter<FlexibleItem>(data, rows, columns, clickCallback) {
    override fun getCoverBitmap(
        position: Int,
        expectWidth: Float,
        expectHeight: Float,
    ): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.icon_shelf_show_mode_16)
    }

    override fun getTitleText(position: Int): String {
        return data[position].name
    }

    override fun getProgressDescriptions(position: Int): String {
        return "Progress: $position"
    }

    override fun getRtModel(position: Int): FlexibleGridView.FlexibleRtModel {
        return FlexibleGridView.FlexibleRtModel()
    }
}

data class FlexibleItem(val name: String)