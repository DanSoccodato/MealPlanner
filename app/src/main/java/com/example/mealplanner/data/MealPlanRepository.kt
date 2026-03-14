package com.example.mealplanner.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealPlanRepository(private val mealPlanDao: MealPlanDao) {
    private val _mealPlans = MutableStateFlow<List<MealPlan>>(emptyList())
    val mealPlans: StateFlow<List<MealPlan>> = _mealPlans.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            mealPlanDao.getAllMealPlans().collect {
                _mealPlans.value = it
            }
        }
    }

    fun addMealPlan(mealPlan: MealPlan) {
        CoroutineScope(Dispatchers.IO).launch {
            mealPlanDao.insertMealPlan(mealPlan)
        }
    }

    fun deleteMealPlan(day: String) {
        CoroutineScope(Dispatchers.IO).launch {
            mealPlanDao.deleteMealPlan(day)
        }
    }

    fun getAllMealPlans(): List<MealPlan> = _mealPlans.value

    fun getMealPlanByDay(day: String): MealPlan? = _mealPlans.value.find { it.day == day }
}
