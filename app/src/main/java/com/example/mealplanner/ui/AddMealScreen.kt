package com.example.mealplanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mealplanner.data.Meal
import com.example.mealplanner.data.MealRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    navController: NavController, 
    mealRepository: MealRepository,
    mealId: Int = -1
) {
    val existingMeal = remember(mealId) { 
        if (mealId != -1) mealRepository.getMealById(mealId) else null 
    }

    var mealName by remember { mutableStateOf(existingMeal?.name ?: "") }
    var ingredients by remember { 
        mutableStateOf(existingMeal?.ingredients ?: listOf("")) 
    }
    
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mealId == -1) "Add Meal" else "Edit Meal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = mealName,
                onValueChange = { mealName = it },
                label = { Text("Meal Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Ingredients:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            ingredients.forEachIndexed { index, ingredient ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ingredient,
                        onValueChange = { newValue ->
                            ingredients = ingredients.toMutableList().also {
                                it[index] = newValue
                            }
                        },
                        label = { Text("Ingredient ${index + 1}") },
                        modifier = Modifier.weight(1f)
                    )

                    if (index == ingredients.size - 1) {
                        IconButton(onClick = { ingredients = ingredients + "" }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                        }
                    } else {
                        IconButton(onClick = { ingredients = ingredients.filterIndexed { i, _ -> i != index } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Ingredient")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val meal = Meal(
                        id = if (mealId == -1) 0 else mealId, 
                        name = mealName, 
                        ingredients = ingredients.filter { it.isNotBlank() }
                    )
                    if (mealId == -1) {
                        mealRepository.addMeal(meal)
                    } else {
                        mealRepository.updateMeal(meal)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = mealName.isNotBlank() && ingredients.any { it.isNotBlank() }
            ) {
                Text(if (mealId == -1) "Save Meal" else "Update Meal")
            }
        }
    }
}
