package com.example.companyanalysis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Company::class], version = 3, exportSchema = false)
abstract class CompanyDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao

    companion object {
        @Volatile
        private var instance: CompanyDatabase? = null

        fun getInstance(context: Context): CompanyDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CompanyDatabase::class.java,
                    "company_database"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
