package com.example.mealplanner.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
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
    var searchQuery by remember { mutableStateOf("") }

    val filteredMealPlans = remember(mealPlans, searchQuery, meals) {
        if (searchQuery.isEmpty()) {
            mealPlans
        } else {
            mealPlans.filter { plan ->
                plan.day.contains(searchQuery, ignoreCase = true) ||
                plan.mealIds.any { id ->
                    val meal = meals.find { m -> m.id == id }
                    meal?.name?.contains(searchQuery, ignoreCase = true) == true
                }
            }
        }
    }

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
        Column(modifier = Modifier.padding(padding)) {
            val interactionSource = remember { MutableInteractionSource() }

            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .height(36.dp),
                    interactionSource = interactionSource,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
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
                        interactionSource = interactionSource,
                        placeholder = { Text("Search plans by day or meal...", fontSize = 16.sp) },
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

            if (filteredMealPlans.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (searchQuery.isEmpty()) "No meal plans yet. Create one!" else "No matching plans found.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredMealPlans) { mealPlan ->
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanItem(
    mealPlan: MealPlan, 
    meals: List<com.example.mealplanner.data.Meal>, 
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mealPlan.day, 
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Show less" else "Show more",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete Plan",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Meals:", style = MaterialTheme.typography.titleSmall)
                        mealPlan.mealIds.forEach { mealId ->
                            val meal = meals.find { it.id == mealId }
                            meal?.let {
                                Text(text = "• ${it.name}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
