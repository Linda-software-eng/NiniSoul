package com.lin.ninisoul

// blueprint for a recipe the USER creates and saves
// photoPath is nullable because not every recipe will have a photo attached
data class SavedRecipe(
    val title: String,
    val ingredients: String,
    val steps: String,
    val tag: String,
    val dateAdded: String,
    val photoPath: String?
)