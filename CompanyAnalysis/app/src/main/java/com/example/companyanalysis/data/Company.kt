package com.example.companyanalysis.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val capitalization: Double
)
