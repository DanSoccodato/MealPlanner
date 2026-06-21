package com.example.mealplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    val mealPlans by mealPlanRepository.mealPlans.collectAsState()
    val existingPlan = remember(initialDay, mealPlans) {
        initialDay?.let { day -> mealPlans.find { it.day == day } }
    }

    var selectedDay by remember { mutableStateOf(initialDay ?: days[0]) }
    var expandedDay by remember { mutableStateOf(false) }
    
    val allMeals by mealRepository.meals.collectAsState()
    val selectedMealIds = remember { mutableStateListOf<Int>() }

    var searchQuery by remember { mutableStateOf("") }
    val filteredMeals = remember(allMeals, searchQuery) {
        if (searchQuery.isEmpty()) {
            allMeals
        } else {
            allMeals.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

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
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
            val interactionSource = remember { MutableInteractionSource() }

            Text(text = "Select Day:", style = MaterialTheme.typography.titleSmall, fontSize = 14.sp)
            Box {
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    BasicTextField(
                        value = selectedDay,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .height(36.dp),
                        interactionSource = interactionSource,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    ) { innerTextField ->
                        OutlinedTextFieldDefaults.DecorationBox(
                            value = selectedDay,
                            innerTextField = innerTextField,
                            enabled = initialDay == null,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = interactionSource,
                            trailingIcon = {
                                if (initialDay == null) {
                                    IconButton(
                                        onClick = { expandedDay = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowDropDown, "Select Day", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            container = {
                                OutlinedTextFieldDefaults.ContainerBox(
                                    enabled = initialDay == null,
                                    isError = false,
                                    interactionSource = interactionSource,
                                    colors = OutlinedTextFieldDefaults.colors(),
                                    shape = OutlinedTextFieldDefaults.shape,
                                    focusedBorderThickness = 1.dp,
                                    unfocusedBorderThickness = 1.dp
                                )
                            }
                        )
                    }
                }
                DropdownMenu(expanded = expandedDay, onDismissRequest = { expandedDay = false }) {
                    days.forEach { day ->
                        DropdownMenuItem(
                            text = { Text(day, fontSize = 14.sp) },
                            onClick = {
                                selectedDay = day
                                expandedDay = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = "Select Meals:", style = MaterialTheme.typography.titleSmall, fontSize = 14.sp)
            
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                val searchInteractionSource = remember { MutableInteractionSource() }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(36.dp),
                    interactionSource = searchInteractionSource,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                ) { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = searchQuery,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = searchInteractionSource,
                        placeholder = { Text("Search meals...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        "Clear search",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        container = {
                            OutlinedTextFieldDefaults.ContainerBox(
                                enabled = true,
                                isError = false,
                                interactionSource = searchInteractionSource,
                                colors = OutlinedTextFieldDefaults.colors(),
                                shape = OutlinedTextFieldDefaults.shape,
                                focusedBorderThickness = 1.dp,
                                unfocusedBorderThickness = 1.dp
                            )
                        }
                    )
                }
            }
            
            if (allMeals.isEmpty()) {
                Text("No meals available. Create some first!", fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
            } else if (filteredMeals.isEmpty()) {
                Text("No meals match your search.", fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                items(filteredMeals) { meal ->
                    Card(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .fillMaxWidth()
                            .clickable {
                                if (selectedMealIds.contains(meal.id)) {
                                    selectedMealIds.remove(meal.id)
                                } else {
                                    selectedMealIds.add(meal.id)
                                }
                            }
                    ) {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedMealIds.contains(meal.id),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedMealIds.add(meal.id)
                                        else selectedMealIds.remove(meal.id)
                                    },
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = meal.name, 
                                    modifier = Modifier.padding(start = 8.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
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
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = selectedMealIds.isNotEmpty()
            ) {
                Text("Save Plan")
            }
        }
    }
}
