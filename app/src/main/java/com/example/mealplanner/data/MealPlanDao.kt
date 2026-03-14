package com.example.mealplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans")
    fun getAllMealPlans(): Flow<List<MealPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan)

    @Query("DELETE FROM meal_plans WHERE day = :day")
    suspend fun deleteMealPlan(day: String)

    @Query("SELECT * FROM meal_plans WHERE day = :day")
    suspend fun getMealPlanByDay(day: String): MealPlan?
}
