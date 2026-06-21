package com.example.mealplanner.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mealplanner.data.IngredientRepository
import com.example.mealplanner.data.Meal
import com.example.mealplanner.data.MealRepository
import com.example.mealplanner.utils.CsvExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealListScreen(
    navController: NavController, 
    mealRepository: MealRepository,
    ingredientRepository: IngredientRepository
) {
    val meals by mealRepository.meals.collectAsState(initial = emptyList<Meal>())
    val ingredients by ingredientRepository.ingredients.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val exportDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri: Uri? ->
            uri?.let {
                CsvExporter.exportUnifiedData(context, it, meals, ingredients)
            }
        }
    )

    val importDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val (importedMeals, importedIngredients) = CsvExporter.importUnifiedData(context, it)
                
                // 1. Import Ingredients first
                importedIngredients.forEach { importedIng ->
                    val exists = ingredients.any { it.name.equals(importedIng.name.trim(), ignoreCase = true) }
                    if (!exists) {
                        ingredientRepository.addIngredient(importedIng)
                    }
                }

                // 2. Import Meals
                importedMeals.forEach { importedMeal ->
                    val isDuplicate = meals.any { existingMeal ->
                        existingMeal.name.equals(importedMeal.name.trim(), ignoreCase = true) &&
                        existingMeal.ingredients.map { it.lowercase().trim() }.sorted() == 
                        importedMeal.ingredients.map { it.lowercase().trim() }.sorted()
                    }
                    if (!isDuplicate) {
                        mealRepository.addMeal(importedMeal)
                    }
                }
            }
        }
    )

    val filteredMeals = remember(meals, searchQuery) {
        if (searchQuery.isEmpty()) {
            meals
        } else {
            meals.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal List") },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Import Data (Unified)") },
                                onClick = {
                                    showMenu = false
                                    importDataLauncher.launch("text/*")
                                },
                                leadingIcon = { Icon(Icons.Default.FileUpload, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Data (Unified)") },
                                onClick = {
                                    showMenu = false
                                    exportDataLauncher.launch("meal_planner_data.csv")
                                },
                                leadingIcon = { Icon(Icons.Default.FileDownload, null) }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addMeal") }) {
                Text("Add")
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
                        placeholder = { Text("Search meals...", fontSize = 16.sp) },
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

            if (filteredMeals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (searchQuery.isEmpty()) "No meals yet. Add one!" else "No meals found matching your search.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredMeals) { meal ->
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealItem(meal: Meal, onDelete: () -> Unit, onClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

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
                            text = meal.name, 
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Hide ingredients" else "Show ingredients",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete, 
                            contentDescription = "Delete Meal",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Ingredients:", style = MaterialTheme.typography.titleSmall)
                        meal.ingredients.forEach { ingredient ->
                            Text(text = "• $ingredient", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
