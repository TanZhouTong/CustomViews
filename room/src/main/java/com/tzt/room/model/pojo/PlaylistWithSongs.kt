package com.tzt.room.model.pojo

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.tzt.room.model.Playlist
import com.tzt.room.model.PlaylistSongCrossRef
import com.tzt.room.model.Song

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/6 13:55
 *
 * playlist与其对应的songs关系数据连接表（POJO）
 * 不对应数据库表，但能将查询结果组合起来
 */
data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",// 指的是 Playlist 实体的 'id' 字段
        entityColumn = "id",// 指的是 Song 实体的 'id' 字段
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            // 当连接表中的字段和实体中的key字段column命名不一致时(一致时可省略)，需显示指定,如下：
            parentColumn = "playlistId", // 1. 在连接表中，用 'playlistId' 列去匹配父实体 (Playlist) 的 'id' 列
            entityColumn = "songId" // 2.在连接表中，用 'songId' 列去匹配子实体 (Song) 的 'id' 列
        )
    ) val songs: List<Song>,
)