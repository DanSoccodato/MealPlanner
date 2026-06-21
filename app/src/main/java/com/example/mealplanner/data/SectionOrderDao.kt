package com.example.mealplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionOrderDao {
    @Query("SELECT * FROM section_orders ORDER BY position ASC")
    fun getAllSectionOrders(): Flow<List<SectionOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sectionOrders: List<SectionOrder>)

    @Query("DELETE FROM section_orders")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateAll(sectionOrders: List<SectionOrder>) {
        deleteAll()
        insertAll(sectionOrders)
    }
}
