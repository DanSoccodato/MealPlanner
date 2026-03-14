package com.example.mealplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    mealRepository: MealRepository,
    initialDay: String? = null
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Custom")
    
    // Collect the plans as state to ensure we can find the existing one
    val mealPlans by mealPlanRepository.mealPlans.collectAsState()
    val existingPlan = remember(initialDay, mealPlans) {
        initialDay?.let { day -> mealPlans.find { it.day == day } }
    }

    var selectedDay by remember { mutableStateOf(initialDay ?: days[0]) }
    var expandedDay by remember { mutableStateOf(false) }
    
    val allMeals by mealRepository.meals.collectAsState()
    val selectedMealIds = remember { mutableStateListOf<Int>() }

    // Update selectedMealIds when existingPlan is loaded
    LaunchedEffect(existingPlan) {
        if (existingPlan != null) {
            selectedMealIds.clear()
            selectedMealIds.addAll(existingPlan.mealIds)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialDay == null) "Create Meal Plan" else "Edit Meal Plan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(text = "Select Day:", style = MaterialTheme.typography.titleMedium)
            Box {
                OutlinedTextField(
                    value = selectedDay,
                    onValueChange = { },
                    readOnly = true,
                    enabled = initialDay == null, // Can't change day if editing
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (initialDay == null) {
                            IconButton(onClick = { expandedDay = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Select Day")
                            }
                        }
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
