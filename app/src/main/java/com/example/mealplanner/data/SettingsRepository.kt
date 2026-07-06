package com.example.mealplanner.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("meal_planner_prefs", Context.MODE_PRIVATE)
    
    private val _showSections = MutableStateFlow(sharedPreferences.getBoolean("show_sections", true))
    val showSections: StateFlow<Boolean> = _showSections.asStateFlow()

    private val _expandedMeals = MutableStateFlow(
        sharedPreferences.getStringSet("expanded_meals", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    )
    val expandedMeals: StateFlow<Set<Int>> = _expandedMeals.asStateFlow()

    private val _expandedMealPlans = MutableStateFlow(
        sharedPreferences.getStringSet("expanded_meal_plans", emptySet()) ?: emptySet()
    )
    val expandedMealPlans: StateFlow<Set<String>> = _expandedMealPlans.asStateFlow()

    fun setShowSections(show: Boolean) {
        _showSections.value = show
        sharedPreferences.edit().putBoolean("show_sections", show).apply()
    }

    fun toggleMealExpansion(mealId: Int) {
        val current = _expandedMeals.value.toMutableSet()
        if (current.contains(mealId)) {
            current.remove(mealId)
        } else {
            current.add(mealId)
        }
        _expandedMeals.value = current
        sharedPreferences.edit().putStringSet("expanded_meals", current.map { it.toString() }.toSet()).apply()
    }

    fun toggleMealPlanExpansion(day: String) {
        val current = _expandedMealPlans.value.toMutableSet()
        if (current.contains(day)) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _expandedMealPlans.value = current
        sharedPreferences.edit().putStringSet("expanded_meal_plans", current).apply()
    }
}
