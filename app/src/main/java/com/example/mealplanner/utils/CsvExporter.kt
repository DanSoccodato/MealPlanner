package com.example.mealplanner.utils

import android.content.Context
import android.net.Uri
import com.example.mealplanner.data.Ingredient
import com.example.mealplanner.data.Meal
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.StringBuilder

object CsvExporter {
    fun exportUnifiedData(context: Context, uri: Uri, meals: List<Meal>, ingredients: List<Ingredient>) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                // Header
                writer.write("Meal,Ingredient,Aisle\n")
                
                // 1. Export Meals and their ingredients
                meals.forEach { meal ->
                    if (meal.ingredients.isEmpty()) {
                        writer.write("${escapeCsv(meal.name)},,\n")
                    } else {
                        meal.ingredients.forEach { ingName ->
                            val aisle = ingredients.find { it.name.equals(ingName, ignoreCase = true) }?.aisle ?: "General"
                            writer.write("${escapeCsv(meal.name)},${escapeCsv(ingName)},${escapeCsv(aisle)}\n")
                        }
                    }
                }
                
                // 2. Export standalone ingredients (those not in any meal)
                val ingredientsInMeals = meals.flatMap { it.ingredients }.map { it.lowercase().trim() }.toSet()
                ingredients.filter { it.name.lowercase().trim() !in ingredientsInMeals }.forEach { ingredient ->
                    writer.write(",${escapeCsv(ingredient.name)},${escapeCsv(ingredient.aisle)}\n")
                }
            }
        }
    }

    fun importUnifiedData(context: Context, uri: Uri): Pair<List<Meal>, List<Ingredient>> {
        val mealsMap = mutableMapOf<String, MutableList<String>>()
        val ingredientsMap = mutableMapOf<String, String>() // Name -> Aisle

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLine() // Skip header
                var line: String? = reader.readLine()
                while (line != null) {
                    val parts = parseCsvLine(line)
                    if (parts.size >= 3) {
                        val mealName = parts[0].trim()
                        val ingName = parts[1].trim()
                        val aisle = parts[2].trim()

                        if (mealName.isNotEmpty()) {
                            val ingredientList = mealsMap.getOrPut(mealName) { mutableListOf() }
                            if (ingName.isNotEmpty()) {
                                ingredientList.add(ingName)
                            }
                        }
                        
                        if (ingName.isNotEmpty()) {
                            // Map ingredient name to aisle, prefer non-empty aisle if multiple entries exist
                            val existingAisle = ingredientsMap[ingName]
                            if (existingAisle == null || (existingAisle == "General" && aisle.isNotEmpty())) {
                                ingredientsMap[ingName] = if (aisle.isNotEmpty()) aisle else "General"
                            }
                        }
                    }
                    line = reader.readLine()
                }
            }
        }

        val meals = mealsMap.map { (name, ingredients) ->
            Meal(name = name, ingredients = ingredients)
        }
        val ingredients = ingredientsMap.map { (name, aisle) ->
            Ingredient(name = name, aisle = aisle)
        }

        return Pair(meals, ingredients)
    }

    fun exportMealsToCsv(context: Context, uri: Uri, meals: List<Meal>) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write("ID,Name,Ingredients\n")
                meals.forEach { meal ->
                    writer.write("${meal.id},${escapeCsv(meal.name)},${escapeCsv(meal.ingredients.joinToString(", "))}\n")
                }
            }
        }
    }

    fun importMealsFromCsv(context: Context, uri: Uri): List<Meal> {
        val importedMeals = mutableListOf<Meal>()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLine()
                var line: String? = reader.readLine()
                while (line != null) {
                    val parts = parseCsvLine(line)
                    if (parts.size >= 3) {
                        val name = parts[1]
                        val ingredients = parts[2].split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        importedMeals.add(Meal(name = name, ingredients = ingredients))
                    }
                    line = reader.readLine()
                }
            }
        }
        return importedMeals
    }

    fun exportIngredientsToCsv(context: Context, uri: Uri, ingredients: List<Ingredient>) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write("ID,Name,Aisle\n")
                ingredients.forEach { ingredient ->
                    writer.write("${ingredient.id},${escapeCsv(ingredient.name)},${escapeCsv(ingredient.aisle)}\n")
                }
            }
        }
    }

    fun importIngredientsFromCsv(context: Context, uri: Uri): List<Ingredient> {
        val importedIngredients = mutableListOf<Ingredient>()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLine()
                var line: String? = reader.readLine()
                while (line != null) {
                    val parts = parseCsvLine(line)
                    if (parts.size >= 3) {
                        importedIngredients.add(Ingredient(name = parts[1], aisle = parts[2]))
                    }
                    line = reader.readLine()
                }
            }
        }
        return importedIngredients
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString())
                current.setLength(0)
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
