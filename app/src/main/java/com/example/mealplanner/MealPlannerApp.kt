package com.example.mealplanner

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mealplanner.data.AppDatabase
import com.example.mealplanner.data.GroceryRepository
import com.example.mealplanner.data.MealPlanRepository
import com.example.mealplanner.data.MealRepository
import com.example.mealplanner.ui.AddMealPlanScreen
import com.example.mealplanner.ui.AddMealScreen
import com.example.mealplanner.ui.GroceryListScreen
import com.example.mealplanner.ui.MealListScreen
import com.example.mealplanner.ui.MealPlanScreen

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object MealList : Screen("mealList", "Meals", Icons.Default.Restaurant)
    object MealPlan : Screen("mealPlan", "Plan", Icons.AutoMirrored.Filled.List)
    object GroceryList : Screen("groceryList", "Groceries", Icons.Default.ShoppingCart)
}

@Composable
fun MealPlannerApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val mealRepository = remember { MealRepository(database.mealDao()) }
    val mealPlanRepository = remember { MealPlanRepository(database.mealPlanDao()) }
    val groceryRepository = remember { GroceryRepository(database.groceryDao()) }

    val navController = rememberNavController()

    val items = listOf(
        Screen.MealList,
        Screen.MealPlan,
        Screen.GroceryList,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
                MealListScreen(navController, mealRepository) 
            }
            composable("addMeal") { 
                AddMealScreen(navController, mealRepository) 
            }
            composable(
                route = "editMeal/{mealId}",
                arguments = listOf(navArgument("mealId") { type = NavType.IntType })
            ) { backStackEntry ->
                val mealId = backStackEntry.arguments?.getInt("mealId") ?: -1
                AddMealScreen(navController, mealRepository, mealId)
            }
            composable(Screen.MealPlan.route) { 
                MealPlanScreen(navController, mealPlanRepository, mealRepository, groceryRepository)
            }
            composable("addMealPlan") {
                AddMealPlanScreen(navController, mealPlanRepository, mealRepository)
            }
            composable(Screen.GroceryList.route) { 
                GroceryListScreen(navController, mealPlanRepository, mealRepository, groceryRepository)
            }
        }
    }
}
