package com.lin.ninisoul

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

// Fridge Cook tab - this is one of my 3 main screens
// It's wired up in nav_graph.xml with id "fridgeCookFragment", which matches
// the same id in bottom_nav_menu.xml. MainActivity.kt is what actually
// connects the bottom nav clicks to switching screens, using setupWithNavController
class FridgeCookFragment : Fragment() {

    // this just tells Android which xml file to draw for this screen
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fridge_cook, container, false)
    }

    // once the screen is actually drawn, I can start grabbing the views and adding logic
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // grabbing my views from fragment_fridge_cook.xml using their ids
        val etIngredients = view.findViewById<EditText>(R.id.et_ingredients)
        val btnFindRecipes = view.findViewById<Button>(R.id.btn_find_recipes)
        val tvResults = view.findViewById<TextView>(R.id.tv_results)
        val tvMatchBadge = view.findViewById<TextView>(R.id.tv_match_badge)

        // everything below runs when the user taps "Find Recipes"
        btnFindRecipes.setOnClickListener {

            // grabbing whatever the user typed and making it all lowercase
            // so "Tomatoes" and "tomatoes" don't get treated as different things
            val typedText = etIngredients.text.toString().lowercase()

            // splitting what they typed by commas to get a list of separate ingredients
            val userIngredients = typedText.split(",")
                .map { it.trim() } // clean up extra spaces
                .filter { it.isNotEmpty() } // in case they typed a stray comma

            // if they didn't type anything, just stop here and tell them
            if (userIngredients.isEmpty()) {
                tvResults.text = "Please type at least one ingredient."
                tvMatchBadge.visibility = View.GONE
                return@setOnClickListener
            }

            // checking my RecipeData list for any recipe that matches at least
            // one of the ingredients the user typed in. Using "any" instead of "all"
            // here on purpose, so results show up easily instead of needing a perfect match
            val matchingRecipes = RecipeData.recipes.filter { recipe ->
                recipe.matchIngredients.any { required ->
                    userIngredients.any { typed -> typed.contains(required) || required.contains(typed) }
                }
            }

            if (matchingRecipes.isEmpty()) {
                // nothing matched, so hide the badge and show a friendly message
                tvMatchBadge.visibility = View.GONE
                tvResults.text = "No recipes found with those ingredients. Try adding a few more."
            } else {
                // got results - show the little green badge with the count
                tvMatchBadge.visibility = View.VISIBLE
                tvMatchBadge.text = "${matchingRecipes.size} matches found"

                // using SpannableStringBuilder here instead of a normal string because
                // I need to make the recipe name bold/bigger and the labels bold too,
                // which a plain String can't do on its own
                val resultText = SpannableStringBuilder()

                for (recipe in matchingRecipes) {

                    // recipe name - making it bold and a bit bigger so it stands out
                    val startName = resultText.length
                    resultText.append(recipe.name)
                    resultText.setSpan(StyleSpan(Typeface.BOLD), startName, resultText.length, 0)
                    resultText.setSpan(RelativeSizeSpan(1.3f), startName, resultText.length, 0)
                    resultText.append("\n\n")

                    // "Ingredients" label in bold
                    val startIngredientsLabel = resultText.length
                    resultText.append("Ingredients")
                    resultText.setSpan(StyleSpan(Typeface.BOLD), startIngredientsLabel, resultText.length, 0)
                    resultText.append("\n")

                    // my recipe ingredients are stored as one string separated by semicolons
                    // (look at RecipeData.kt) so I split that here to show each one
                    // as its own bullet point line instead of one long paragraph
                    val ingredientsList = recipe.fullIngredients.split(";").map { it.trim() }
                    for (ingredient in ingredientsList) {
                        resultText.append("• $ingredient\n")
                    }
                    resultText.append("\n")

                    // "Method" label in bold, then the actual cooking steps
                    val startStepsLabel = resultText.length
                    resultText.append("Method")
                    resultText.setSpan(StyleSpan(Typeface.BOLD), startStepsLabel, resultText.length, 0)
                    resultText.append("\n")
                    resultText.append(recipe.steps)
                    resultText.append("\n\n")

                    // light gray divider so multiple recipes don't blend into each other
                    val dividerStart = resultText.length
                    resultText.append("─────────────────────\n\n")
                    resultText.setSpan(ForegroundColorSpan(Color.parseColor("#CCCCCC")), dividerStart, resultText.length, 0)
                }

                // finally push all that formatted text into the results TextView
                tvResults.text = resultText
            }
        }
    }
}