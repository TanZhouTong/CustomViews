package com.tzt.pageview.nonscroll

//import com.tzt.pageview.nonscroll.WrapperGridAdapter.PageGridItemAdapter.GridViewHold
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.recyclerview.widget.RecyclerView
import com.tzt.pageview.R
import com.tzt.pageview.nonscroll.WrapperGridAdapter.GridViewHold

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 11:05
 * grid布局时的adapter
 */
class WrapperGridAdapter(
    val context: Context,
    val gridAdapter: GridItemAdapter<*>,
    val data: List<*>,
    val itemsCountInPage: Int,
    val columns: Int,
) : RecyclerView.Adapter<GridViewHold>() {
    companion object {
        const val TAG = "WrapperGridAdapter"
    }

    inner class GridViewHold(view: View) : RecyclerView.ViewHolder(view) {

        val gridView: GridView = view.findViewById<GridView>(R.id.grid_item_container).apply {
            Log.d(TAG, "GridViewHold init()")
            numColumns = columns
            gravity = Gravity.CENTER
            stretchMode = GridView.STRETCH_SPACING
            adapter = gridAdapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHold {
        Log.d(TAG, "onCreateViewHolder()")
        return GridViewHold(
            LayoutInflater.from(context).inflate(R.layout.layout_grid_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: GridViewHold, position: Int) {
        Log.d(TAG, "onBindViewHolder(): gridView is:${holder.gridView}")
        // pass， or refresh the data
        // 根据position,喂对应的数据
        val adapter = holder.gridView.adapter as GridItemAdapter<*>
        adapter.submitData(getCurrentPageData(position))
    }

    /**
     * 这里返回的就是GridView的个数了
     * */
    override fun getItemCount(): Int {
        val total = data.size
        if (total == 0) {
            Log.d(TAG, "getItemCount is -> 0")
            return 0
        }
        val count = total / itemsCountInPage + if (total % itemsCountInPage == 0) 0 else 1
        Log.d(TAG, "getItemCount is -> $count")
        return count
    }

    private fun getCurrentPageData(currentPage: Int): List<*> {
        Log.d(TAG, "getCurrentPageData currentPage:$currentPage")
        val from = currentPage * itemsCountInPage
        val nextPageFirst = (currentPage + 1) * itemsCountInPage
        val to = if (nextPageFirst < data.size - 1) nextPageFirst else data.size
        Log.d(TAG, "getCurrentPageData : from:$from -> to:$to")
        return data.subList(from, to)
    }
}