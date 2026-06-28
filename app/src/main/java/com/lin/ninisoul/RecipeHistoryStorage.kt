package com.lin.ninisoul

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RecipeHistoryStorage {

    private const val FILE_NAME = "history_logs_v2.txt" // Upgraded filename to handle IDs cleanly

    // Appends the meal ID, title, and today's date stamp into our text file log
    fun logMealSelection(context: Context, mealId: Int, mealName: String) {
        val file = File(context.filesDir, FILE_NAME)
        val dateSignature = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        // Save format: ID|Name|Date
        val recordLine = "$mealId|$mealName|$dateSignature\n"

        val outputStream = FileOutputStream(file, true)
        outputStream.write(recordLine.toByteArray())
        outputStream.close()
    }

    fun getHistoryRecords(context: Context): List<HistoryItem> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()

        val itemsList = mutableListOf<HistoryItem>()

        file.useLines { lines ->
            lines.forEach { line ->
                if (line.contains("|")) {
                    val splitParts = line.split("|")
                    if (splitParts.size >= 3) {
                        itemsList.add(
                            HistoryItem(
                                mealId = splitParts[0].toIntOrNull() ?: 1,
                                mealName = splitParts[1],
                                dateCooked = splitParts[2]
                            )
                        )
                    }
                }
            }
        }
        return itemsList.reversed() // Newest entries up top
    }

    fun clearHistory(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}

data class HistoryItem(
    val mealId: Int,
    val mealName: String,
    val dateCooked: String
)