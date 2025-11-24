package com.tzt.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/24 15:14
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE playlist ADD COLUMN description TEXT") // DEFAULT '暂无描述' NOT NULL
    }
}