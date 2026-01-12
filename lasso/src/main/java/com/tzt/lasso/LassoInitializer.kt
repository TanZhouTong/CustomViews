package com.tzt.lasso

import android.content.Context
import androidx.startup.Initializer

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2026/1/12 14:21
 */
class LassoInitializer: Initializer<LassoInitializer>  {
    override fun create(context: Context): LassoInitializer {
        CustomLassoHelper(context).showLassoView()
        return this
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> {
        return emptyList()
    }
}