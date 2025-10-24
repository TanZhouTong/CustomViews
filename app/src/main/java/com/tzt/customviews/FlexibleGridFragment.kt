package com.tzt.customviews

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tzt.pageview.gridview.FlexibleGridView
import com.tzt.pageview.nonscroll.TextPagerIndicator

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/17 10:17
 */
class FlexibleGridFragment : Fragment(), FlexibleGridView.IClickCallback {
    lateinit var flexibleGridView: FlexibleGridView
    lateinit var pagerIndicator: TextPagerIndicator

    companion object {
        const val TAG = "GridFragment"
        fun getInstance(): FlexibleGridFragment {
            return FlexibleGridFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_flexible_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView(view)
        Log.d(TAG, "onViewCreated success")
    }

    var rows = 3
    var columns = 4

    @SuppressLint("NotifyDataSetChanged")
    private fun initView(view: View) {
        flexibleGridView = view.findViewById(R.id.flexible)
        pagerIndicator = view.findViewById(R.id.page_indicator)
        flexibleGridView.adapter =
            FlexibleAdapter(requireActivity(), mutableListOf<FlexibleItem>().apply {
                for (i in 0..100) {
                    add(FlexibleItem("item:$i"))
                }
            }, rows, columns, this)
        pagerIndicator.setupWithPagerView(flexibleGridView)
        // test currentPage
        flexibleGridView.post {
            flexibleGridView.currentPage = 4
        }
    }

    var count = 0
    override fun onSingleTapUp(position: Int) {
        Log.d(TAG, "OnClick...$position")
        // 测试局部刷新
        (flexibleGridView.adapter as? FlexibleAdapter)?.notifyPositionDataChange(
            position,
            FlexibleItem("局部刷新: ${count++}")
        )

        // 测试新的adapter
        flexibleGridView.adapter =
            FlexibleAdapter(requireActivity(), mutableListOf<FlexibleItem>().apply {
                for (i in 0..80) {
                    add(FlexibleItem("add:$i"))
                }
            }, rows, columns, this)
    }

    override fun onLongPress(position: Int) {
        Log.d(TAG, "onLongPress...$position")
        rows++
        columns++
        /*flexibleGridView.adapter =
            FlexibleAdapter(requireActivity(), mutableListOf<FlexibleItem>().apply {
                for (i in 0..100) {
                    if (i % 10 == 0) add(FlexibleItem(""))
                    else add(FlexibleItem("item:$i"))
                }
            }, rows, columns, this)*/
        // 测试行列修改
        (flexibleGridView.adapter as? FlexibleAdapter)?.notifyConfigurationChange(rows, columns)
    }
}