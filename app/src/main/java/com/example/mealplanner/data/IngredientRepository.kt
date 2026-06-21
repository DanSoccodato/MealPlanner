package com.example.mealplanner.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IngredientRepository(private val ingredientDao: IngredientDao) {
    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            ingredientDao.getAllIngredients().collect {
                _ingredients.value = it
            }
        }
    }

    suspend fun getIngredientByName(name: String): Ingredient? {
        return ingredientDao.getIngredientByName(name)
    }

    fun addIngredient(ingredient: Ingredient) {
        CoroutineScope(Dispatchers.IO).launch {
            ingredientDao.insertIngredient(ingredient)
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        CoroutineScope(Dispatchers.IO).launch {
            ingredientDao.updateIngredient(ingredient)
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        CoroutineScope(Dispatchers.IO).launch {
            ingredientDao.deleteIngredient(ingredient)
        }
    }

    fun getAllIngredients(): List<Ingredient> = _ingredients.value
}
