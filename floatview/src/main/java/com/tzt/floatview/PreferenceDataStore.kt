package com.tzt.floatview

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/5 19:44
 */

val Context.preferenceDs: DataStore<Preferences> by preferencesDataStore(name = "test_pref")

val EXAMPLE_COUNTER = intPreferencesKey("count")