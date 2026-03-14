package com.example.mealplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_items ORDER BY position ASC")
    fun getAllItems(): Flow<List<GroceryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GroceryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<GroceryItem>)

    @Query("DELETE FROM grocery_items WHERE name = :name")
    suspend fun deleteItem(name: String)

    @Query("DELETE FROM grocery_items")
    suspend fun deleteAll()
}
