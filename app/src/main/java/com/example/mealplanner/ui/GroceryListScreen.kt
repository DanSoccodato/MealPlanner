package com.example.mealplanner.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mealplanner.data.GroceryRepository
import com.example.mealplanner.data.IngredientRepository
import com.example.mealplanner.data.MealPlanRepository
import com.example.mealplanner.data.MealRepository
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    navController: NavController,
    mealPlanRepository: MealPlanRepository,
    mealRepository: MealRepository,
    groceryRepository: GroceryRepository,
    ingredientRepository: IngredientRepository
) {
    val mealPlans by mealPlanRepository.mealPlans.collectAsState()
    val meals by mealRepository.meals.collectAsState()
    val groceryItemsState by groceryRepository.items.collectAsState()
    val allIngredients by ingredientRepository.ingredients.collectAsState()

    // 1. Sync meal ingredients with database to ensure they have positions
    LaunchedEffect(mealPlans, meals) {
        val ingredients = mutableListOf<String>()
        mealPlans.forEach { mealPlan ->
            mealPlan.mealIds.forEach { mealId ->
                val meal = meals.find { it.id == mealId }
                meal?.let { ingredients.addAll(it.ingredients) }
            }
        }
        groceryRepository.syncMealIngredients(ingredients.distinct())
    }

    // 2. Derive the list from database state
    val dbOrderedItems by remember {
        derivedStateOf {
            val ingredientsInPlans = mutableListOf<String>()
            mealPlans.forEach { mealPlan ->
                mealPlan.mealIds.forEach { mealId ->
                    val meal = meals.find { it.id == mealId }
                    meal?.let { ingredientsInPlans.addAll(it.ingredients) }
                }
            }
            
            groceryItemsState
                .filter { it.name in ingredientsInPlans || it.isExtra }
                .filter { !it.isRemoved }
                .sortedBy { it.position }
                .map { it.name }
        }
    }

    // 3. Maintain a local mutable state for smooth dragging
    val localOrderedItems = remember { mutableStateListOf<String>() }
    
    // Sync local state when database items change (initial load or new items)
    LaunchedEffect(dbOrderedItems) {
        // Only update if the content actually changed (avoiding loop during drag)
        if (localOrderedItems.toList() != dbOrderedItems) {
            localOrderedItems.clear()
            localOrderedItems.addAll(dbOrderedItems)
        }
    }

    // State for reorderable list
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        localOrderedItems.add(to.index, localOrderedItems.removeAt(from.index))
    }, canDragOver = { _, _ -> true })

    LaunchedEffect(localOrderedItems.toList()) {
        groceryRepository.updateItemPositions(localOrderedItems.toList())
    }

    var newIngredient by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grocery List") },
                actions = {
                    TextButton(onClick = { groceryRepository.reset() }) {
                        Text("Reset", fontSize = 14.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val inputInteractionSource = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    BasicTextField(
                        value = newIngredient,
                        onValueChange = { newIngredient = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        interactionSource = inputInteractionSource,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    ) { innerTextField ->
                        TextFieldDefaults.OutlinedTextFieldDecorationBox(
                            value = newIngredient,
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = inputInteractionSource,
                            placeholder = { Text("Add extra item...", fontSize = 14.sp) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            container = {
                                OutlinedTextFieldDefaults.ContainerBox(
                                    enabled = true,
                                    isError = false,
                                    interactionSource = inputInteractionSource,
                                    colors = OutlinedTextFieldDefaults.colors(),
                                    shape = OutlinedTextFieldDefaults.shape,
                                    focusedBorderThickness = 1.dp,
                                    unfocusedBorderThickness = 1.dp
                                )
                            }
                        )
                    }
                }
                IconButton(
                    onClick = {
                        if (newIngredient.isNotBlank()) {
                            groceryRepository.addExtraIngredient(newIngredient)
                            newIngredient = ""
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp))
                }
            }

            if (localOrderedItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No grocery items yet.", fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    state = state.listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .reorderable(state)
                ) {
                    items(localOrderedItems, key = { it }) { item ->
                        ReorderableItem(state, key = item) { isDragging ->
                            val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
                            
                            val itemData = groceryItemsState.find { it.name == item }
                            val isBought = itemData?.isBought == true
                            val aisle = allIngredients.find { it.name.equals(item, ignoreCase = true) }?.aisle
                            
                            GroceryItem(
                                item = item,
                                aisle = aisle,
                                isBought = isBought,
                                elevation = elevation.value,
                                onToggleBought = { groceryRepository.toggleBought(item) },
                                onDelete = { groceryRepository.removeIngredient(item) },
                                modifier = Modifier.detectReorderAfterLongPress(state)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryItem(
    item: String, 
    aisle: String?,
    isBought: Boolean,
    elevation: Dp,
    onToggleBought: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .fillMaxWidth()
            .shadow(elevation),
        colors = CardDefaults.cardColors(
            containerColor = if (isBought) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Reorder",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                
                Checkbox(
                    checked = isBought,
                    onCheckedChange = { onToggleBought() },
                    modifier = Modifier.size(32.dp)
                )
                
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        style = LocalTextStyle.current.copy(
                            textDecoration = if (isBought) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isBought) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (aisle != null) {
                        Text(
                            text = aisle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        )
                    }
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete", 
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
