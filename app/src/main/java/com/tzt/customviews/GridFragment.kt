package com.tzt.customviews

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tzt.pageview.nonscroll.PageRecyclerView
import com.tzt.pageview.nonscroll.PagerGridLayoutManager
import com.tzt.pageview.nonscroll.TextPagerIndicator

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 16:46
 */
class GridFragment(): Fragment() {
    lateinit var pageView: PageRecyclerView
    lateinit var pagerIndicator: TextPagerIndicator

    companion object {
        const val TAG = "GridFragment"
        fun getInstance() : GridFragment {
            return GridFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_grid_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView(view)
        Log.d(TAG, "onViewCreated success")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView(view: View) {
        pagerIndicator = view.findViewById(R.id.page_indicator)
        pageView = view.findViewById<PageRecyclerView>(R.id.page_view).apply {
            layoutManager = PagerGridLayoutManager(requireActivity(), 3, 3)
            setGridAdapter(MyGridAdapter(requireActivity()), mutableListOf<ItemData>().apply {
                for(index in 0..100) {
                    add(ItemData("test[$index]"))
                }
            })
        }
        pagerIndicator.setupWithPagerView(pageView)
    }
}