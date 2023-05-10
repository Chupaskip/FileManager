package com.example.filemanager.database

import androidx.room.*
import java.io.File

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("select * from files")
    suspend fun getSavedFiles(): List<FileEntity>

    @Query("SELECT * FROM files WHERE hashCodeFile = :hashCodeFile")
    suspend fun getFileByHashCode(hashCodeFile: Int): FileEntity?
}