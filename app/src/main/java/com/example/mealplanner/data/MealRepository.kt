package com.example.mealplanner.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealRepository(private val mealDao: MealDao) {
    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            mealDao.getAllMeals().collect {
                _meals.value = it
            }
        }
    }

    fun addMeal(meal: Meal) {
        CoroutineScope(Dispatchers.IO).launch {
            mealDao.insertMeal(meal)
        }
    }

    fun updateMeal(updatedMeal: Meal) {
        CoroutineScope(Dispatchers.IO).launch {
            mealDao.updateMeal(updatedMeal)
        }
    }

    fun deleteMeal(mealId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val meal = mealDao.getMealById(mealId)
            meal?.let { mealDao.deleteMeal(it) }
        }
    }

    fun getAllMeals(): List<Meal> = _meals.value

    fun getMealById(id: Int): Meal? = _meals.value.find { it.id == id }
}
