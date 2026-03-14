package com.example.mealplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mealplanner.data.Meal
import com.example.mealplanner.data.MealRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealListScreen(navController: NavController, mealRepository: MealRepository) {
    val meals = mealRepository.getAllMeals()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meal List") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addMeal") }) {
                Text("Add")
            }
        }
    ) { padding ->
        if (meals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No meals yet. Add one!")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(meals) { meal ->
                    MealItem(
                        meal = meal, 
                        onDelete = { mealRepository.deleteMeal(meal.id) },
                        onClick = { navController.navigate("editMeal/${meal.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun MealItem(meal: Meal, onDelete: () -> Unit, onClick: () -> Unit) {
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
                Text(text = meal.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Ingredients:", style = MaterialTheme.typography.titleSmall)
                meal.ingredients.forEach { ingredient ->
                    Text(text = "• $ingredient")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Meal")
            }
        }
    }
}
