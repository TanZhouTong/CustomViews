package com.tzt.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/6 12:29
 */
@Entity(tableName="playlist")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null // 新增测试数据库升级
)
