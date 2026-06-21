package com.example.mealplanner.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mealplanner.data.IngredientRepository
import com.example.mealplanner.data.SectionOrder
import com.example.mealplanner.data.SectionOrderRepository
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionOrderScreen(
    ingredientRepository: IngredientRepository,
    sectionOrderRepository: SectionOrderRepository,
    onOpenDrawer: () -> Unit
) {
    val ingredients by ingredientRepository.ingredients.collectAsState()
    val savedOrders by sectionOrderRepository.sectionOrders.collectAsState()

    // Extract unique sections from ingredients
    val allSections = remember(ingredients) {
        ingredients.map { it.section }.distinct().sorted()
    }

    // Local state for reordering
    val localSections = remember { mutableStateListOf<String>() }

    // Sync local state with saved orders and new sections
    LaunchedEffect(savedOrders, allSections) {
        val orderedSections = savedOrders.map { it.section }.filter { it in allSections }
        val newSections = allSections.filter { it !in orderedSections }
        
        val combined = orderedSections + newSections
        if (localSections.toList() != combined) {
            localSections.clear()
            localSections.addAll(combined)
        }
    }

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        localSections.add(to.index, localSections.removeAt(from.index))
    })

    // Save changes when the list is reordered
    LaunchedEffect(localSections.toList()) {
        val orders = localSections.mapIndexed { index, section ->
            SectionOrder(section, index)
        }
        sectionOrderRepository.updateSectionOrders(orders)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Section Ordering") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "Drag to reorder sections. This affects the Grocery List sorting.",
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(state)
            ) {
                items(localSections, key = { it }) { section ->
                    ReorderableItem(state, key = section) { isDragging ->
                        val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
                        
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .fillMaxWidth()
                                .shadow(elevation.value)
                                .detectReorderAfterLongPress(state)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(text = section, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
