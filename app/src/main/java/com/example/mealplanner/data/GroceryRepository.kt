package com.example.mealplanner.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroceryRepository(private val groceryDao: GroceryDao) {
    private val _items = MutableStateFlow<List<GroceryItem>>(emptyList())
    val items: StateFlow<List<GroceryItem>> = _items.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            groceryDao.getAllItems().collect {
                _items.value = it
            }
        }
    }

    fun addExtraIngredient(ingredient: String) {
        val trimmed = ingredient.trim()
        if (trimmed.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                val currentMaxPos = _items.value.maxOfOrNull { it.position } ?: 0
                groceryDao.insertItem(GroceryItem(trimmed, isExtra = true, isBought = false, isRemoved = false, position = currentMaxPos + 1))
            }
        }
    }

    fun forceSync(mealIngredients: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentItems = _items.value
            val updatedItems = mutableListOf<GroceryItem>()
            var maxPos = currentItems.maxOfOrNull { it.position } ?: 0

            // 1. Add or un-remove items that are in the meal plans
            mealIngredients.forEach { name ->
                val existing = currentItems.find { it.name == name }
                if (existing != null) {
                    // If it was removed, un-remove it
                    if (existing.isRemoved) {
                        updatedItems.add(existing.copy(isRemoved = false))
                    }
                } else {
                    // Add new
                    updatedItems.add(GroceryItem(name, isExtra = false, isBought = false, isRemoved = false, position = ++maxPos))
                }
            }

            // 2. Remove items that are no longer in the meal plans (unless they were manually added)
            currentItems.forEach { item ->
                if (!item.isExtra && !item.isRemoved && item.name !in mealIngredients) {
                    updatedItems.add(item.copy(isRemoved = true))
                }
            }
            
            if (updatedItems.isNotEmpty()) {
                groceryDao.insertItems(updatedItems)
            }
        }
    }

    fun removeIngredient(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = _items.value.find { it.name == name }
            if (existing != null) {
                groceryDao.insertItem(existing.copy(isRemoved = true))
            } else {
                val maxPos = _items.value.maxOfOrNull { it.position } ?: 0
                groceryDao.insertItem(GroceryItem(name, isExtra = false, isBought = false, isRemoved = true, position = maxPos + 1))
            }
        }
    }

    fun toggleBought(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = _items.value.find { it.name == name }
            if (existing != null) {
                groceryDao.insertItem(existing.copy(isBought = !existing.isBought))
            }
        }
    }

    fun updateItemPositions(itemsInOrder: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val updatedItems = itemsInOrder.mapIndexedNotNull { index, name ->
                val existing = _items.value.find { it.name == name }
                existing?.copy(position = index)
            }
            if (updatedItems.isNotEmpty()) {
                groceryDao.insertItems(updatedItems)
            }
        }
    }

    fun isBought(name: String): Boolean = _items.value.find { it.name == name }?.isBought ?: false
    fun isRemoved(name: String): Boolean = _items.value.find { it.name == name }?.isRemoved ?: false
    
    fun reset() {
        CoroutineScope(Dispatchers.IO).launch {
            groceryDao.deleteAll()
        }
    }
}
