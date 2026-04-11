package com.example.bd_students.data

import android.content.Context
import androidx.room.Room
import com.example.bd_students.R

object AppDatabaseProvider {

    private const val DATABASE_NAME = "school.db"

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context.applicationContext).also { instance = it }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        SimpleDBHelper(context).copyDatabaseFromRawIfNeeded(DATABASE_NAME, R.raw.school)
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
    }
}
