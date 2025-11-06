package com.tzt.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/6 12:28
 */

@Entity(tableName = "song")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)