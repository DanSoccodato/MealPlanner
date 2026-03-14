package com.example.mealplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mealplanner.data.GroceryRepository
import com.example.mealplanner.data.MealPlan
import com.example.mealplanner.data.MealPlanRepository
import com.example.mealplanner.data.MealRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    navController: NavController,
    mealPlanRepository: MealPlanRepository,
    mealRepository: MealRepository,
    groceryRepository: GroceryRepository
) {
    val mealPlans by mealPlanRepository.mealPlans.collectAsState()
    val meals by mealRepository.meals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Plan") },
                actions = {
                    IconButton(onClick = {
                        val allIngredients = mutableListOf<String>()
                        mealPlans.forEach { plan ->
                            plan.mealIds.forEach { id ->
                                val meal = meals.find { it.id == id }
                                meal?.let { allIngredients.addAll(it.ingredients) }
                            }
                        }
                        groceryRepository.forceSync(allIngredients.distinct())
                    }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync Groceries")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addMealPlan") }) {
                Text("Create Plan")
            }
        }
    ) { padding ->
        if (mealPlans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No meal plans yet. Create one!")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(mealPlans) { mealPlan ->
                    MealPlanItem(
                        mealPlan = mealPlan, 
                        meals = meals,
                        onDelete = { mealPlanRepository.deleteMealPlan(mealPlan.day) },
                        onClick = { navController.navigate("editMealPlan/${mealPlan.day}") }
                    )
                }
            }
        }
    }
}

@Composable
fun MealPlanItem(
    mealPlan: MealPlan, 
    meals: List<com.example.mealplanner.data.Meal>, 
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mealPlan.day, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Meals:", style = MaterialTheme.typography.titleSmall)
                mealPlan.mealIds.forEach { mealId ->
                    val meal = meals.find { it.id == mealId }
                    meal?.let {
                        Text(text = "• ${it.name}")
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Plan")
            }
        }
    }
}
