package com.tzt.room.model

import androidx.room.Entity
import androidx.room.Index

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/6 13:53
 * 这是连接表，不是实体类，反映的是多对多的关系
 * 连接规则
 */
@Entity(
    tableName = "song_cross_playlist",
    primaryKeys = ["songId", "playlistId"],
    indices = [Index(value = ["songId"]), Index(value = ["playlistId"])]
)
data class PlaylistSongCrossRef(
    val songId: Int,
    val playlistId: Int,
)