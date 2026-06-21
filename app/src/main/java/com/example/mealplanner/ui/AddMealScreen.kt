package com.example.mealplanner.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.mealplanner.data.Ingredient
import com.example.mealplanner.data.IngredientRepository
import com.example.mealplanner.data.Meal
import com.example.mealplanner.data.MealRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    navController: NavController, 
    mealRepository: MealRepository,
    ingredientRepository: IngredientRepository,
    mealId: Int = -1
) {
    val scope = rememberCoroutineScope()
    val existingMeal = remember(mealId) { 
        if (mealId != -1) mealRepository.getMealById(mealId) else null 
    }

    var mealName by remember { mutableStateOf(existingMeal?.name ?: "") }
    var ingredientNames by remember { 
        mutableStateOf(existingMeal?.ingredients ?: listOf("")) 
    }
    
    val allIngredients by ingredientRepository.ingredients.collectAsState()
    
    var showSectionDialog by remember { mutableStateOf(false) }
    var ingredientToSetSection by remember { mutableStateOf<String?>(null) }
    var newSectionValue by remember { mutableStateOf("General") }

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
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            CompactTextField(
                value = mealName,
                onValueChange = { mealName = it },
                placeholder = "Meal Name",
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .fillMaxWidth()
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(
                            text = "Ingredients:", 
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))

                        ingredientNames.forEachIndexed { index, name ->
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        CompactTextField(
                                            value = name,
                                            onValueChange = { newValue ->
                                                ingredientNames = ingredientNames.toMutableList().also {
                                                    it[index] = newValue
                                                }
                                            },
                                            placeholder = "Ingredient ${index + 1}",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index == ingredientNames.size - 1) {
                                                ingredientNames = ingredientNames + ""
                                            } else {
                                                ingredientNames = ingredientNames.filterIndexed { i, _ -> i != index }
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (index == ingredientNames.size - 1) Icons.Default.Add else Icons.Default.Delete,
                                            contentDescription = if (index == ingredientNames.size - 1) "Add" else "Remove",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                // Suggestions
                                val currentName = name
                                if (currentName.isNotBlank() && allIngredients.none { it.name.equals(currentName, ignoreCase = true) }) {
                                    val suggestions = allIngredients.filter { 
                                        it.name.contains(currentName, ignoreCase = true) 
                                    }.take(3)
                                    
                                    if (suggestions.isNotEmpty()) {
                                        Row(modifier = Modifier.padding(start = 12.dp)) {
                                            suggestions.forEach { suggestion ->
                                                SuggestionChip(
                                                    onClick = {
                                                        ingredientNames = ingredientNames.toMutableList().also {
                                                            it[index] = suggestion.name
                                                        }
                                                    },
                                                    label = { Text(suggestion.name, fontSize = 12.sp) },
                                                    modifier = Modifier.padding(end = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalIngredients = ingredientNames.filter { it.isNotBlank() }
                    
                    // Check for new ingredients and prompt for section if needed
                    val newIngredients = finalIngredients.filter { ingName ->
                        allIngredients.none { it.name.equals(ingName, ignoreCase = true) }
                    }
                    
                    if (newIngredients.isNotEmpty()) {
                        ingredientToSetSection = newIngredients.first()
                        newSectionValue = "General"
                        showSectionDialog = true
                    } else {
                        saveMealAndPop(mealId, mealName, finalIngredients, mealRepository, navController)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = mealName.isNotBlank() && ingredientNames.any { it.isNotBlank() }
            ) {
                Text(if (mealId == -1) "Save Meal" else "Update Meal", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSectionDialog && ingredientToSetSection != null) {
        AlertDialog(
            onDismissRequest = { showSectionDialog = false },
            title = { Text("New Ingredient: ${ingredientToSetSection}") },
            text = {
                Column {
                    Text("Please specify the section category:")
                    Spacer(modifier = Modifier.height(8.dp))
                    CompactTextField(
                        value = newSectionValue,
                        onValueChange = { newSectionValue = it },
                        placeholder = "Section (e.g., Produce, Dairy)"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val currentIng = ingredientToSetSection!!
                    val currentSection = newSectionValue
                    
                    ingredientRepository.addIngredient(Ingredient(name = currentIng, section = currentSection))
                    
                    // Check if there are more new ingredients
                    val finalIngredients = ingredientNames.filter { it.isNotBlank() }
                    // Note: allIngredients is a state, it might not update immediately after addIngredient call
                    // but since we are in a Composable and using State, it should trigger recomposition.
                    // However, for the loop logic we might need to manually exclude the one we just added.
                    val remainingNew = finalIngredients.filter { name ->
                        allIngredients.none { it.name.equals(name, ignoreCase = true) } &&
                        !name.equals(currentIng, ignoreCase = true)
                    }
                    
                    if (remainingNew.isNotEmpty()) {
                        ingredientToSetSection = remainingNew.first()
                        newSectionValue = "General"
                    } else {
                        showSectionDialog = false
                        saveMealAndPop(mealId, mealName, finalIngredients, mealRepository, navController)
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun saveMealAndPop(
    mealId: Int,
    mealName: String,
    ingredients: List<String>,
    mealRepository: MealRepository,
    navController: NavController
) {
    val meal = Meal(
        id = if (mealId == -1) 0 else mealId, 
        name = mealName, 
        ingredients = ingredients
    )
    if (mealId == -1) {
        mealRepository.addMeal(meal)
    } else {
        mealRepository.updateMeal(meal)
    }
    navController.popBackStack()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
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
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                placeholder = { Text(placeholder, fontSize = 16.sp) },
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
}
