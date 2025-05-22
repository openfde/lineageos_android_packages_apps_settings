package com.android.settings

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.settings.compatible.CompatibleList
import com.android.settings.compatible.CompatibleListDao

@Database(
    entities = [ CompatibleList::class],
    version = 1,
    exportSchema = false
)
abstract class SettingsDb : RoomDatabase() {
    abstract fun compatibleListDao(): CompatibleListDao

    companion object {
        private var instance: SettingsDb? = null

        @Synchronized
        fun getInstance(context: Context): SettingsDb {
            return instance ?: Room.databaseBuilder(
                context,
                SettingsDb::class.java,
                "settings.db"
            )
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}