package com.example.mealplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "section_orders")
data class SectionOrder(
    @PrimaryKey val section: String,
    val position: Int
)
