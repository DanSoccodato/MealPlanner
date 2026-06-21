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
        ) {
            // Meal Name Input - Styled like the Search Bar in MealListScreen
            Spacer(modifier = Modifier.height(4.dp))
            CompactTextField(
                value = mealName,
                onValueChange = { mealName = it },
                placeholder = "Meal Name",
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Ingredients Card - Styled like the MealItem card in MealListScreen
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
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))

                        ingredients.forEachIndexed { index, ingredient ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                CompactTextField(
                                    value = ingredient,
                                    onValueChange = { newValue ->
                                        ingredients = ingredients.toMutableList().also {
                                            it[index] = newValue
                                        }
                                    },
                                    placeholder = "Ingredient ${index + 1}",
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        if (index == ingredients.size - 1) {
                                            ingredients = ingredients + ""
                                        } else {
                                            ingredients = ingredients.filterIndexed { i, _ -> i != index }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (index == ingredients.size - 1) Icons.Default.Add else Icons.Default.Delete,
                                        contentDescription = if (index == ingredients.size - 1) "Add" else "Remove",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = mealName.isNotBlank() && ingredients.any { it.isNotBlank() }
            ) {
                Text(if (mealId == -1) "Save Meal" else "Update Meal", fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
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
                fontSize = 14.sp,
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
                placeholder = { Text(placeholder, fontSize = 14.sp) },
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
