package com.example.mealplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mealplanner.data.MealPlan
import com.example.mealplanner.data.MealPlanRepository
import com.example.mealplanner.data.MealRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealPlanScreen(
    navController: NavController,
    mealPlanRepository: MealPlanRepository,
    mealRepository: MealRepository
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDay by remember { mutableStateOf(days[0]) }
    var expandedDay by remember { mutableStateOf(false) }
    
    val allMeals = mealRepository.getAllMeals()
    val selectedMealIds = remember { mutableStateListOf<Int>() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Meal Plan") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(text = "Select Day:", style = MaterialTheme.typography.titleMedium)
            Box {
                OutlinedTextField(
                    value = selectedDay,
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, "Select Day", Modifier.clickable { expandedDay = true })
                    }
                )
                DropdownMenu(expanded = expandedDay, onDismissRequest = { expandedDay = false }) {
                    days.forEach { day ->
                        DropdownMenuItem(
                            text = { Text(day) },
                            onClick = {
                                selectedDay = day
                                expandedDay = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Select Meals:", style = MaterialTheme.typography.titleMedium)
            
            if (allMeals.isEmpty()) {
                Text("No meals available. Create some first!")
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allMeals) { meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedMealIds.contains(meal.id)) {
                                    selectedMealIds.remove(meal.id)
                                } else {
                                    selectedMealIds.add(meal.id)
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedMealIds.contains(meal.id),
                            onCheckedChange = { checked ->
                                if (checked) selectedMealIds.add(meal.id)
                                else selectedMealIds.remove(meal.id)
                            }
                        )
                        Text(text = meal.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedMealIds.isNotEmpty()) {
                        mealPlanRepository.addMealPlan(MealPlan(selectedDay, selectedMealIds.toList()))
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMealIds.isNotEmpty()
            ) {
                Text("Save Plan")
            }
        }
    }
}
