package com.example.companyanalysis.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies ORDER BY capitalization DESC, name ASC")
    fun observeCompanies(): Flow<List<Company>>

    @Query("SELECT COUNT(*) FROM companies")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(companies: List<Company>)

    @Query("SELECT name FROM companies")
    suspend fun getAllNames(): List<String>

    @Query("SELECT * FROM companies WHERE name LIKE '%' || :substring || '%' ORDER BY capitalization DESC, name ASC")
    suspend fun getCompaniesBySubstring(substring: String): List<Company>

    @Query("DELETE FROM companies WHERE name LIKE '%' || :substring || '%'")
    suspend fun deleteBySubstring(substring: String)

    @Query("SELECT COALESCE(SUM(capitalization), 0) FROM companies")
    fun observeTotalCapitalization(): Flow<Double>

    @Query(
        """
        SELECT COUNT(*)
        FROM companies
        WHERE capitalization > (SELECT AVG(capitalization) FROM companies)
        """
    )
    fun observeAboveAverageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM companies WHERE name < 'A'")
    fun observeEnglishNamesCount(): Flow<Int>

    @Query("SELECT name FROM companies ORDER BY capitalization DESC, name ASC LIMIT 1")
    fun observeMaxCapitalizationCompany(): Flow<String?>

    @Query("SELECT name FROM companies ORDER BY LENGTH(name) DESC, name ASC LIMIT 1")
    fun observeLongestNameCompany(): Flow<String?>
}
