package com.example.bd_students.data

import android.content.Context
import androidx.annotation.RawRes
import java.io.FileOutputStream

class SimpleDBHelper(
    private val context: Context
) {
    fun copyDatabaseFromRawIfNeeded(databaseName: String, @RawRes rawResId: Int) {
        val databaseFile = context.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()
        if (databaseFile.exists()) {
            context.deleteDatabase(databaseName)
        }

        context.resources.openRawResource(rawResId).use { inputStream ->
            FileOutputStream(databaseFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
