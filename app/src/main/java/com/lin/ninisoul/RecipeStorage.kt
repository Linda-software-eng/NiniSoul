package com.lin.ninisoul

import android.content.Context
import java.io.File

// handles saving and loading my recipes using simple text files instead of a database
// each recipe becomes its own .txt file inside a private "recipes" folder
object RecipeStorage {

    private fun getRecipesDir(context: Context): File {
        val dir = File(context.filesDir, "recipes")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    // saves one recipe as a new file
    // now storing 6 fields separated by "|||" instead of 5, since I added photoPath
    // if there's no photo, I just save the word "none" so I know to skip it when reading back
    fun saveRecipe(context: Context, recipe: SavedRecipe) {
        val dir = getRecipesDir(context)
        val fileName = "recipe_${System.currentTimeMillis()}.txt"
        val file = File(dir, fileName)

        val photoValue = recipe.photoPath ?: "none"
        val content = "${recipe.title}|||${recipe.ingredients}|||${recipe.steps}|||${recipe.tag}|||${recipe.dateAdded}|||$photoValue"
        file.writeText(content)
    }

    // reads every saved recipe file and turns it back into a list of SavedRecipe objects
    fun getAllRecipes(context: Context): List<SavedRecipe> {
        val dir = getRecipesDir(context)
        val files = dir.listFiles() ?: return emptyList()

        val recipes = mutableListOf<SavedRecipe>()
        for (file in files) {
            val content = file.readText()
            val parts = content.split("|||")
            if (parts.size == 6) {
                // if I saved "none" earlier, I turn it back into a real null here
                val photo = if (parts[5] == "none") null else parts[5]
                recipes.add(
                    SavedRecipe(
                        title = parts[0],
                        ingredients = parts[1],
                        steps = parts[2],
                        tag = parts[3],
                        dateAdded = parts[4],
                        photoPath = photo
                    )
                )
            }
        }
        return recipes.sortedByDescending { it.dateAdded }
    }

    // deletes one recipe by finding the file that matches its exact content
    fun deleteRecipe(context: Context, recipe: SavedRecipe) {
        val dir = getRecipesDir(context)
        val files = dir.listFiles() ?: return
        for (file in files) {
            val content = file.readText()
            if (content.startsWith(recipe.title + "|||")) {
                file.delete()
                break
            }
        }
    }
    // updates an existing recipe by deleting the old version and saving the new one
// I match the OLD recipe by its original title, then write the NEW updated details
    fun updateRecipe(context: Context, oldRecipe: SavedRecipe, updatedRecipe: SavedRecipe) {
        deleteRecipe(context, oldRecipe)
        saveRecipe(context, updatedRecipe)
    }
}