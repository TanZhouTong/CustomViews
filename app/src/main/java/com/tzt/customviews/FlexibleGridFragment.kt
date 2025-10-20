package com.tzt.customviews

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tzt.pageview.gridview.FlexibleGridView

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/17 10:17
 */
class FlexibleGridFragment : Fragment(), FlexibleGridView.IClickCallback {
    lateinit var flexibleGridView: FlexibleGridView

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
        flexibleGridView.adapter = FlexibleAdapter(requireActivity(), mutableListOf<FlexibleItem>().apply {
            for (i in 0..100) {
                add(FlexibleItem("item:$i"))
            }
        }, rows, columns, this)
    }

    override fun onSingleTapUp(positionInPage: Int) {
        Log.d(TAG, "OnClick...$positionInPage")
        rows++
        columns++
        flexibleGridView.adapter = FlexibleAdapter(requireActivity(), mutableListOf<FlexibleItem>().apply {
            for (i in 0..100) {
                add(FlexibleItem("item:$i"))
            }
        }, rows, columns, this)
    }

    override fun onLongPress(positionInPage: Int) {
        Log.d(TAG, "onLongPress...$positionInPage")
        rows--
        columns--
        flexibleGridView.adapter = FlexibleAdapter(requireActivity(), mutableListOf<FlexibleItem>().apply {
            for (i in 0..100) {
                add(FlexibleItem("item:$i"))
            }
        }, rows, columns, this)
    }
}