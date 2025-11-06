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
    indices = [Index(value = ["songId"]), Index(value = ["playlistId"])]    // 分别为此连接表添加两个索引，方便查询（否则会编译不过，提示会有性能问题）
)
data class PlaylistSongCrossRef(
    val songId: Int,
    val playlistId: Int,
)

/**
 *
 * 警告: The column playlistId in the junction entity com.tzt.room.model.
 * PlaylistSongCrossRef is being used to resolve a relationship but it is not covered by any index.
 * This might cause a full table scan when resolving the relationship,
 * it is highly advised to create an index that covers this column. private final int playlistId = 0;
 *
 * 这个警告是 Room 帮助你写出高性能数据库代码的体现。
 * 原因：连接表中的外键列（playlistId 和 songId）经常被用于查询，但它们没有被索引，可能导致大数据量下的查询性能低下（ 全表扫描） 。
 * 解决方案：在 @Entity 注解中使用 indices 属性为这两个外键列分别创建索引。 这是一个非常好的实践，建议在所有用作外键的列上都考虑添加索引
 * */