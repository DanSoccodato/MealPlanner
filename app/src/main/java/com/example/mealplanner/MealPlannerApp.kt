package com.example.mealplanner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.mealplanner.data.*
import com.example.mealplanner.ui.*
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object MealList : Screen("mealList", "Meals", Icons.Default.Restaurant)
    object MealPlan : Screen("mealPlan", "Plan", Icons.AutoMirrored.Filled.List)
    object GroceryList : Screen("groceryList", "Groceries", Icons.Default.ShoppingCart)
    object IngredientList : Screen("ingredientList", "Ingredients", Icons.AutoMirrored.Filled.ListAlt)
    object SectionOrder : Screen("sectionOrder", "Section ordering", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val mealRepository = remember { MealRepository(database.mealDao()) }
    val mealPlanRepository = remember { MealPlanRepository(database.mealPlanDao()) }
    val groceryRepository = remember { GroceryRepository(database.groceryDao()) }
    val ingredientRepository = remember { IngredientRepository(database.ingredientDao()) }
    val sectionOrderRepository = remember { SectionOrderRepository(database.sectionOrderDao()) }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Handle back button to close drawer if it's open
    if (drawerState.isOpen) {
        BackHandler {
            scope.launch { drawerState.close() }
            Unit
        }
    }

    val drawerItems = listOf(
        Screen.MealList,
        Screen.MealPlan,
        Screen.GroceryList,
        Screen.IngredientList,
        Screen.SectionOrder
    )

    val onOpenDrawer: () -> Unit = { 
        scope.launch { drawerState.open() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        scope.launch { drawerState.close() }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Close Menu")
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Meal Planner",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                drawerItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationDrawerItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val bottomItems = listOf(Screen.MealList, Screen.MealPlan, Screen.GroceryList)
                    bottomItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.MealList.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.MealList.route) {
                    MealListScreen(navController, mealRepository, ingredientRepository, onOpenDrawer)
                }
                composable("addMeal") {
                    AddMealScreen(navController, mealRepository, ingredientRepository)
                }
                composable(
                    route = "editMeal/{mealId}",
                    arguments = listOf(navArgument("mealId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val mealId = backStackEntry.arguments?.getInt("mealId") ?: -1
                    AddMealScreen(navController, mealRepository, ingredientRepository, mealId)
                }
                composable(Screen.MealPlan.route) {
                    MealPlanScreen(navController, mealPlanRepository, mealRepository, groceryRepository, onOpenDrawer)
                }
                composable("addMealPlan") {
                    AddMealPlanScreen(navController, mealPlanRepository, mealRepository)
                }
                composable(
                    route = "editMealPlan/{day}",
                    arguments = listOf(navArgument("day") { type = NavType.StringType })
                ) { backStackEntry ->
                    val day = backStackEntry.arguments?.getString("day")
                    AddMealPlanScreen(navController, mealPlanRepository, mealRepository, day)
                }
                composable(Screen.GroceryList.route) {
                    GroceryListScreen(navController, mealPlanRepository, mealRepository, groceryRepository, ingredientRepository, sectionOrderRepository, onOpenDrawer)
                }
                composable(Screen.IngredientList.route) {
                    IngredientListScreen(ingredientRepository, onOpenDrawer)
                }
                composable(Screen.SectionOrder.route) {
                    SectionOrderScreen(ingredientRepository, sectionOrderRepository, onOpenDrawer)
                }
            }
        }
    }
}
