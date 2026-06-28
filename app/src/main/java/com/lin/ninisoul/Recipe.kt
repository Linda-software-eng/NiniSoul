package com.lin.ninisoul

data class Recipe(
    val name: String,
    val matchIngredients: List<String>,
    val fullIngredients: String,
    val steps: String
)