package com.example.mealplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey val day: String,
    val mealIds: List<Int>
)
