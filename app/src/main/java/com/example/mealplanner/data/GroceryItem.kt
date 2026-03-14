package com.example.mealplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey val name: String,
    val isExtra: Boolean,
    val isBought: Boolean,
    val isRemoved: Boolean,
    val position: Int = 0
)
