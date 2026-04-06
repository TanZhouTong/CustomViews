package com.tzt.guideview

import android.view.View
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

/**
 * @author tanzhoutong
 * @date 2026/4/6
 * description: todo 相关描述
 */
class GuideReference(val key: Long, view: GuideView, queue: ReferenceQueue<in GuideView>) :
    WeakReference<GuideView>(view, queue) {
}