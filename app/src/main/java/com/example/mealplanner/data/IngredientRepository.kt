package com.example.mealplanner.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IngredientRepository(private val ingredientDao: IngredientDao) {
    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val updateJobs = mutableMapOf<Int, Job>()
    private val pendingUpdates = mutableSetOf<Int>()

    init {
        repositoryScope.launch {
            ingredientDao.getAllIngredients().collect { dbIngredients ->
                withContext(Dispatchers.Main) {
                    val currentItems = _ingredients.value.associateBy { it.id }
                    _ingredients.value = dbIngredients.map { dbIng ->
                        if (dbIng.id in pendingUpdates) {
                            currentItems[dbIng.id] ?: dbIng
                        } else {
                            dbIng
                        }
                    }
                }
            }
        }
    }

    suspend fun getIngredientByName(name: String): Ingredient? {
        return ingredientDao.getIngredientByName(name)
    }

    fun addIngredient(ingredient: Ingredient) {
        repositoryScope.launch(Dispatchers.IO) {
            ingredientDao.insertIngredient(ingredient)
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        // Mark as pending to prevent database emissions from overwriting local state
        pendingUpdates.add(ingredient.id)
        
        // Optimistic update: update local state immediately for responsiveness
        val currentList = _ingredients.value
        val index = currentList.indexOfFirst { it.id == ingredient.id }
        if (index != -1) {
            val newList = currentList.toMutableList()
            newList[index] = ingredient
            _ingredients.value = newList
        }

        // Cancel previous update job for this ingredient to avoid race conditions
        updateJobs[ingredient.id]?.cancel()
        updateJobs[ingredient.id] = repositoryScope.launch(Dispatchers.IO) {
            try {
                ingredientDao.updateIngredient(ingredient)
                // Hold pending state for a short duration to allow database emissions to settle
                delay(1000)
            } finally {
                withContext(NonCancellable + Dispatchers.Main) {
                    pendingUpdates.remove(ingredient.id)
                }
            }
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        repositoryScope.launch(Dispatchers.IO) {
            ingredientDao.deleteIngredient(ingredient)
        }
    }

    fun getAllIngredients(): List<Ingredient> = _ingredients.value
}
