package com.lin.ninisoul

// Added a simple id number property so the app can match the meal to meal1, meal2, etc.
data class Meal(
    val id: Int,
    val name: String,
    val description: String
)