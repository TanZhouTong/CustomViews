package com.tzt.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.tzt.room.model.Playlist
import com.tzt.room.model.PlaylistSongCrossRef
import com.tzt.room.model.Song
import com.tzt.room.model.pojo.PlaylistWithSongs
import com.tzt.room.model.pojo.SongWithPlaylist
import kotlinx.coroutines.flow.Flow

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/6 12:33
 */
@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addPlaylist(playlist: Playlist)

    @Query("select * from song")
    fun querySong(): Flow<List<Song>>

    @Query("select * from playlist")
    fun queryPlaylist(): Flow<List<Playlist>>

    @Query("delete from song where id = :songId")
    suspend fun deleteSong(songId: Int)

    @Query("delete from playlist where id = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)

    // 下面的就是连接表的一些操作了
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    /**
     * 将song(歌曲)从指定的playlist(播放列表)中删除此歌曲
     * */
    @Query("delete from song_cross_playlist where songId = :songId and playlistId = :playlistId")
    suspend fun deleteSongFromPlaylist(songId: Int, playlistId: Int)

    /**
     * 删除此song的所有关联数据
     * 1.删除连接表
     * 2.删除song表
     * */
    @Transaction
    @Query("DELETE FROM song_cross_playlist WHERE songId = :songId")
    suspend fun deleteSongAll(songId: Int) {
        deleteSong(songId)
    }

    /**
     * 删除此playlist的所有关联数据
     * 1.删除连接表中的指定playlist数据
     * 2.删除playlist表中的指定播放列表数据
     * */
    @Transaction
    @Query("DELETE FROM song_cross_playlist WHERE playlistId = :playlistId")
    suspend fun deletePlaylistAll(playlistId: Int) {
        deletePlaylist(playlistId)
    }

    /**
     * 查询song以及所在的playlist集合
     * 1.先查询Query字段（获取Song）
     * 2.通过song实体和连接表获取playlist集合
     * 3.将查询结果组合起来
     * */
    @Transaction
    @Query("select * from song where id = :songId")
    fun querySongWithPlaylist(songId: Int): Flow<SongWithPlaylist?>

    @Transaction
    @Query("select * from playlist where id = :playlistId")
    fun queryPlaylistWithSong(playlistId: Int): Flow<PlaylistWithSongs?>
}