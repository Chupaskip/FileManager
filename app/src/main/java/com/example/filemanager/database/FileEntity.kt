package com.example.filemanager.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey
    val hashCodeFile: Int,
    val path: String,
    val lastModified: Long
)
fun File.toFileEntity(): FileEntity {
    return FileEntity(
        this.hashCode(),
        this.absolutePath,
        this.lastModified()
    )
}

fun FileEntity.toFile(): File {
    return File(this.path)
}