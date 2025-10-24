package com.tzt.customviews

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tzt.pageview.gridview.UserGridView
import com.tzt.pageview.nonscroll.TextPagerIndicator

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/24 15:03
 */
class UserGridFragment : Fragment(), UserGridView.IClickCallback {
    lateinit var userGridView: UserGridView
    lateinit var pagerIndicator: TextPagerIndicator

    companion object {
        const val TAG = "UserGridFragment"
        fun getInstance(): UserGridFragment {
            return UserGridFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_user_grid_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView(view)
        Log.d(FlexibleGridFragment.Companion.TAG, "onViewCreated success")
    }

    var rows = 3
    var columns = 3

    @SuppressLint("NotifyDataSetChanged")
    private fun initView(view: View) {
        userGridView = view.findViewById(R.id.flexible)
        pagerIndicator = view.findViewById(R.id.page_indicator)
        userGridView.adapter =
            UserAdapter(requireActivity(), mutableListOf<UserInfo>().apply {
                for (i in 0..50) {
                    add(UserInfo("baidu-[$i]", R.drawable.drawable_disk_baidu_icon_c))
                    add(UserInfo("oneDrive-[$i]", R.drawable.drawable_disk_onedrive_icon_1))
                }
            }, rows, columns, this)
        pagerIndicator.setupWithPagerView(userGridView)
        // test currentPage
        userGridView.post {
            userGridView.currentPage = 4
        }
    }

    var count = 0
    override fun onSingleTapUp(position: Int) {
        Log.d(TAG, "OnClick...$position")
        // 测试新的adapter
        /*userGridView.adapter =
            UserAdapter(requireActivity(), mutableListOf<UserInfo>().apply {
                for (i in 0..80) {
                    add(UserInfo("baidu[$i]", R.drawable.drawable_disk_baidu_icon_c))
                    add(UserInfo("oneDrive[$i]", R.drawable.drawable_disk_onedrive_icon_1))
                }
            }, rows, columns, this)*/

        // 测试局部刷新
        (userGridView.adapter as? UserAdapter)?.notifyPositionDataChange(
            position,
            UserInfo(
                "局部刷新: ${count++}",
                R.drawable.drawable_disk_baidu_icon_c,
                AccountInfo("user:${count}")
            )
        )
    }

    override fun onLongPress(position: Int) {
        Log.d(FlexibleGridFragment.Companion.TAG, "onLongPress...$position")
        rows++
        columns++
        // 测试行列修改
        (userGridView.adapter as? UserAdapter)?.notifyConfigurationChange(rows, columns)
    }
}