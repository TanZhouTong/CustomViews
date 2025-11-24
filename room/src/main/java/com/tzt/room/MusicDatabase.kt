package com.tzt.room

import android.content.Context
import androidx.annotation.GuardedBy
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tzt.room.MusicDao
import com.tzt.room.model.Playlist
import com.tzt.room.model.PlaylistSongCrossRef
import com.tzt.room.model.Song
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/6 12:31
 */
@Database(
    entities = [Song::class, Playlist::class, PlaylistSongCrossRef::class],
    version = 2,
    exportSchema = false,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        const val NAME = "music_info.db"

        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    MusicDatabase::class.java,
                    NAME
                ).addMigrations(MIGRATION_1_2)
                    .build().also {
                        INSTANCE = it
                    }
            }
        }
    }
}

val Context.musicDatabase by musicDatabase()

/**
 * 绕圈子主要是为了上手测试下这个流程
 * */
private fun musicDatabase(): ReadOnlyProperty<Context, MusicDatabase> {
    return DatabaseDelegate.getInstance()
}

private class DatabaseDelegate private constructor() : ReadOnlyProperty<Context, MusicDatabase> {

    companion object {
        private val lock: Any = Any()

        @GuardedBy("lock")
        @Volatile
        private var INSTANCE: DatabaseDelegate? = null

        fun getInstance(): DatabaseDelegate {
            return INSTANCE ?: synchronized(lock) {
                INSTANCE ?: DatabaseDelegate().also {
                    INSTANCE = it
                }
            }
        }
    }

    override fun getValue(
        thisRef: Context,
        property: KProperty<*>,
    ): MusicDatabase {
        return MusicDatabase.getInstance(thisRef)
    }
}

