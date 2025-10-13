package com.tzt.pageview.nonscroll

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.tzt.pageview.R
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/10/10 9:56
 */
class PageRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.recyclerview.R.attr.recyclerViewStyle,
) : RecyclerView(context, attrs, defStyleAttr), PagerView {

    companion object {
        const val TAG = "PageRecyclerView"
    }

    init {
        init(context, attrs)
    }

    private val pageChangeListeners by lazy { arrayListOf<PagerView.OnPageChangeListener>() }

    override var currentPage: Int = 0
        set(value) {
            if (field == value) return
            val position = getFirstPositionInPage(value)
            val lm = layoutManager as? LinearLayoutManager
            if (lm != null) {
                lm.scrollToPositionWithOffset(position, 0)
            } else {
                scrollToPosition(position)
            }
            val old = field
            field = value
            notifyPageChange(value, old)
        }

    private var _pageCount: Int = 0
    override val pageCount: Int
        get() {
            val model = layoutManager as? PagerModel ?: throw Utils.layoutManagerError()
            val total = if (adapter is WrapperGridAdapter) {
                (adapter as WrapperGridAdapter).data.size
            }  else adapter?.itemCount ?: return 0
            var itemsInPage = model.itemsInPage
            return total / itemsInPage + if (total % itemsInPage == 0) 0 else 1
        }

    override var isLooper: Boolean = true

    @SuppressLint("CustomViewStyleable", "Recycle")
    private fun init(
        context: Context,
        attrs: AttributeSet?,
    ) {
        context.withStyledAttributes(attrs, R.styleable.PageGridStyleable) {
            isLooper = getBoolean(R.styleable.PageGridStyleable_looper, true)
        }
    }

    private var wrapperLinearAdapter: WrapperLinearAdapter<*>? = null

    override fun setAdapter(adapter: Adapter<*>?) {
        this.adapter?.unregisterAdapterDataObserver(mObserver)
        adapter?.registerAdapterDataObserver(mObserver)

        val lm = layoutManager as? PagerModel
        val wrappedAdapter = if (adapter != null && lm != null) {
            if (wrapperLinearAdapter == null) {
                wrapperLinearAdapter =
                    WrapperLinearAdapter(adapter, lm.itemsInPage)
            } else {
                wrapperLinearAdapter?.adapter = adapter
                wrapperLinearAdapter?.updateItemsInPage(lm.itemsInPage)
            }
            wrapperLinearAdapter
        } else adapter
        super.setAdapter(wrappedAdapter)
        checkPageCount()
    }

    /**
     * 如果为Grid模式，使用这个进行adapter的设置
     * */
    fun setGridAdapter(viewBind: GridItemAdapter.IGridItemViewBind<*>, data: List<*>) {
        val lm = layoutManager as? PagerModel
        if (lm == null) throw Utils.layoutManagerError()
        this.adapter?.unregisterAdapterDataObserver(mObserver)
        WrapperGridAdapter(context, viewBind, data, lm.itemsInPage, lm.columns).apply {
            Log.d(TAG, "setGridAdapter() Start")
            registerAdapterDataObserver(mObserver)
            super.setAdapter(this)
        }
        Log.d(TAG, "setGridAdapter() END")
        checkPageCount()
    }

    override fun getAdapter(): Adapter<*>? {
        val adapter = super.getAdapter()
        Log.d(TAG, "getAdapter -> $adapter")
        return if (adapter is WrapperLinearAdapter<*>) adapter.adapter
        /*else if(adapter is WrapperGridAdapter) adapter*/
        else adapter    // 这里包括了WrapperGridAdapter
    }

    private val mObserver by lazy {
        object : AdapterDataObserver() {
            override fun onChanged() {
                checkPageCount()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                checkPageCount()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                checkPageCount()
            }
        }
    }

    /**
     * page从0开始
     * */
    fun getFirstPositionInPage(page: Int): Int {
        // TODO:根据grid或者linear模式区分
        val adapter = super.getAdapter()
        if (adapter is WrapperGridAdapter) {
            return page
        } else {
            val lm = layoutManager as? PagerModel
                ?: throw Utils.layoutManagerError()
            val itemsInPage = lm.itemsInPage
            return page * itemsInPage
        }
    }

    /**
     * page和position都从0开始
     * */
    fun getPageWithPosition(position: Int): Int {
        val lm = layoutManager as? PagerModel ?: throw Utils.layoutManagerError()
        return (position / lm.itemsInPage) + if (position % lm.itemsInPage == 0) 0 else 1
    }

    private fun notifyPageChange(current: Int, previous: Int) {
        synchronized(pageChangeListeners) {
            pageChangeListeners.forEach {
                it.onPageChange(this, current, previous)
            }
        }
    }

    private fun notifyPageCountChanged(pageCount: Int) {
        synchronized(pageChangeListeners) {
            pageChangeListeners.forEach {
                it.onPageCountChange(this, pageCount)
            }
        }
    }

    private fun notifyPageChangeError(errorCode: Int) {
        synchronized(pageChangeListeners) {
            pageChangeListeners.forEach {
                it.onPageChangeError(this, errorCode)
            }
        }
    }

    private fun checkPageCount() {
        Log.d(TAG, "checkPageCount()")
        val pageCount = this.pageCount
        var current = currentPage
        if (current >= pageCount) {
            current = if (pageCount == 0) 0 else pageCount - 1
        }
        currentPage = current

        if (_pageCount != pageCount) {
            notifyPageCountChanged(pageCount)
            _pageCount = pageCount
        }
    }

    override fun next(): Boolean {
        var current = currentPage
        val total = pageCount
        if (current == total - 1 && !isLooper) {
            notifyPageChangeError(PagerView.ERROR_ISLAST)
            return false
        }
        current++
        if (current >= total) {
            current = 0
        }
        currentPage = current
        return true
    }

    override fun previous(): Boolean {
        var current = currentPage
        if (current == 0 && !isLooper) {
            notifyPageChangeError(PagerView.ERROR_ISFIRST)
            return false
        }
        current--
        if (current < 0) {
            current = pageCount - 1
        }
        currentPage = current
        return true
    }

    override fun addOnPageChangeListener(listener: PagerView.OnPageChangeListener) {
        synchronized(pageChangeListeners) {
            if (pageChangeListeners.contains(listener)) return
            pageChangeListeners.add(listener)
        }
    }

    override fun removeOnPageChangeListener(listener: PagerView.OnPageChangeListener) {
        synchronized(pageChangeListeners) {
            pageChangeListeners.remove(listener)
        }
    }

    val nonScroll = PagerViewNonScrollDelegate(this)

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return nonScroll.onInterceptTouchEvent(e) || super.onInterceptTouchEvent(e)
    }
}