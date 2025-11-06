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
 * @Date 2025/11/6 14:18
 * 同 @link SongWithPlaylist
 */
data class SongWithPlaylist(
    @Embedded
    val song: Song,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "songId",
            entityColumn = "playlistId"
        )
    )
    val lists: List<Playlist>
)