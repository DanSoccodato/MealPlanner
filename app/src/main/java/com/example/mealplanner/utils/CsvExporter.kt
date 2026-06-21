package com.example.mealplanner.utils

import android.content.Context
import android.net.Uri
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object CsvExporter {

    fun exportBackup(context: Context, uri: Uri, mealRows: List<List<String>>, ingredientRows: List<List<String>>) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(outputStream).use { zipOut ->
                // Meals CSV
                zipOut.putNextEntry(ZipEntry("meals.csv"))
                writeCsvToStream(zipOut, listOf("Meal", "Ingredient"), mealRows)
                zipOut.closeEntry()

                // Ingredients CSV
                zipOut.putNextEntry(ZipEntry("ingredients.csv"))
                writeCsvToStream(zipOut, listOf("Ingredient", "Section"), ingredientRows)
                zipOut.closeEntry()
            }
        }
    }

    data class BackupData(val mealRows: List<List<String>>, val ingredientRows: List<List<String>>)

    fun importBackup(context: Context, uri: Uri): BackupData {
        val mealRows = mutableListOf<List<String>>()
        val ingredientRows = mutableListOf<List<String>>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "meals.csv" -> mealRows.addAll(readCsvFromStream(zipIn))
                        "ingredients.csv" -> ingredientRows.addAll(readCsvFromStream(zipIn))
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
        }
        return BackupData(mealRows, ingredientRows)
    }

    private fun writeCsvToStream(os: OutputStream, header: List<String>, rows: List<List<String>>) {
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(header.joinToString(",") { escapeCsv(it) } + "\n")
        rows.forEach { row ->
            writer.write(row.joinToString(",") { escapeCsv(it) } + "\n")
        }
        writer.flush()
        // Do not close the writer here, as it would close the underlying ZipOutputStream
    }

    private fun readCsvFromStream(is_: InputStream): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val reader = BufferedReader(InputStreamReader(is_, "UTF-8"))
        // Skip header
        val header = reader.readLine()
        if (header != null) {
            var line = reader.readLine()
            while (line != null) {
                if (line.isNotEmpty()) {
                    result.add(parseCsvLine(line))
                }
                line = reader.readLine()
            }
        }
        return result
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
