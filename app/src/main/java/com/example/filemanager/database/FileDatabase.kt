package com.example.filemanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FileEntity::class],
    version = 2
)
abstract class FileDatabase : RoomDatabase() {
    abstract fun getFileDao(): FileDao

    companion object {
        @Volatile
        private var instance: FileDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                FileDatabase::class.java,
                "file_db.db"
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}