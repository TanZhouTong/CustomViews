package com.tzt.customviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.tzt.pageview.gridview.UserGridView
import androidx.core.graphics.createBitmap

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/24 14:48
 */
class UserAdapter(
    val context: Context,
    data: List<UserInfo>,
    rows: Int,
    columns: Int,
    clickCallback: UserGridView.IClickCallback,
) : UserGridView.Adapter<UserInfo>(data.toMutableList(), rows, columns, clickCallback) {
    companion object {
        const val TAG = "UserAdapter"
    }
    override fun getCoverBitmap(
        position: Int,
        expectWidth: Float,
        expectHeight: Float,
    ): Bitmap? {
        return null
    }

    override fun getIconBitmap(position: Int): Bitmap? {
        runCatching {
            context.parseBitmap(data[position].resId)
//            BitmapFactory.decodeResource(context.resources, data[position].resId)
        }.onSuccess {
            return it
        }.onFailure {
            Log.e(TAG, "getIconBitmap error -> $it")
        }
        return null
    }

    override fun getAvatarBitmap(position: Int): Bitmap? {
        return data[position].accountInfo?.let {
            context.parseBitmap(R.drawable.drawable_user_center_black_icon)
            //BitmapFactory.decodeResource(context.resources, R.drawable.drawable_user_center_black_icon)
        } ?: run {
            context.parseBitmap(R.drawable.drawable_user_center_gray_icon)
            //BitmapFactory.decodeResource(context.resources, R.drawable.drawable_user_center_gray_icon)
        }
    }

    override fun getTitleText(position: Int): String {
        return data[position].name
    }

    override fun getSubTitleText(position: Int): String? {
        return data[position].accountInfo?.username
    }
}

fun Context.parseBitmap(resId: Int) : Bitmap {
    val drawable = this.getDrawable(resId)
    val bitmap = createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

data class AccountInfo(val username: String)
data class UserInfo(val name: String, val resId: Int, val accountInfo: AccountInfo? = null)