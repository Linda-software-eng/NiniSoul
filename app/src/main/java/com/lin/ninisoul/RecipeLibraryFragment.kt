package com.lin.ninisoul

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import java.io.File

// My Cookbook tab - shows every recipe the user has saved using RecipeStorage.kt
// each recipe is built as its own card here in Kotlin, since the number of
// saved recipes is different for every user and changes over time
class RecipeLibraryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val containerRecipes = view.findViewById<LinearLayout>(R.id.container_recipes)
        val tvEmptyState = view.findViewById<TextView>(R.id.tv_empty_state)
        val btnAddRecipe = view.findViewById<Button>(R.id.btn_add_recipe)

        // tapping the + button navigates to my Add Recipe screen
        // using the action I defined earlier in nav_graph.xml
        btnAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_recipeLibrary_to_addRecipe)
        }

        // this builds and shows all the recipe cards
        // I'm calling this from onResume too (further below) so the list refreshes
        // every time I come back to this screen after saving a new recipe
        loadAndDisplayRecipes(containerRecipes, tvEmptyState)
    }

    // this runs every time this screen becomes visible again
    // (e.g. coming back from Add Recipe after saving) so the list always stays up to date
    override fun onResume() {
        super.onResume()
        val containerRecipes = view?.findViewById<LinearLayout>(R.id.container_recipes)
        val tvEmptyState = view?.findViewById<TextView>(R.id.tv_empty_state)
        if (containerRecipes != null && tvEmptyState != null) {
            loadAndDisplayRecipes(containerRecipes, tvEmptyState)
        }
    }

    private fun loadAndDisplayRecipes(container: LinearLayout, emptyState: TextView) {
        // clear out any old cards first so I don't end up with duplicates
        // every time this function runs
        container.removeAllViews()

        val recipes = RecipeStorage.getAllRecipes(requireContext())

        if (recipes.isEmpty()) {
            // show the friendly empty message, nothing else to do
            emptyState.visibility = View.VISIBLE
            return
        }

        emptyState.visibility = View.GONE

        // build one card per saved recipe and add it to the screen
        for (recipe in recipes) {
            val card = buildRecipeCard(recipe)
            container.addView(card)
        }
    }

    // this builds a single recipe card from scratch using Kotlin code
    // instead of a separate xml file, since I'm repeating this same structure
    // for every recipe and it's simpler to build it once here
    private fun buildRecipeCard(recipe: SavedRecipe): CardView {
        val context = requireContext()

        val card = CardView(context)
        card.radius = 28f
        card.cardElevation = 6f
        card.setCardBackgroundColor(Color.WHITE)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.bottomMargin = 28 // pixels of space below each card
        card.layoutParams = cardParams

        // --- NEW CLICK ACTION HOOK ---
        // Tapping anywhere on the recipe card opens up the full details fragment
        card.setOnClickListener {
            val detailBundle = Bundle().apply {
                putString("title", recipe.title)
                putString("ingredients", recipe.ingredients)
                putString("steps", recipe.steps) // safely maps your layout instructions string
                putString("tag", recipe.tag)
                putString("date", recipe.dateAdded)
                putString("imagePath", recipe.photoPath)
            }
            findNavController().navigate(R.id.action_recipeLibrary_to_recipeDetail, detailBundle)
        }

        // everything inside the card stacks vertically: photo, then text details
        val outerColumn = LinearLayout(context)
        outerColumn.orientation = LinearLayout.VERTICAL

        // photo section at the top of the card
        val photoView = ImageView(context)
        val photoParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 420 // fixed height for the photo area
        )
        photoView.layoutParams = photoParams
        photoView.scaleType = ImageView.ScaleType.CENTER_CROP

        if (recipe.photoPath != null && File(recipe.photoPath).exists()) {
            photoView.setImageURI(Uri.fromFile(File(recipe.photoPath)))
        } else {
            // no photo saved, just show a soft green placeholder block instead
            photoView.setBackgroundColor(Color.parseColor("#E8F5E9"))
        }
        outerColumn.addView(photoView)

        // text details section below the photo
        val textColumn = LinearLayout(context)
        textColumn.orientation = LinearLayout.VERTICAL
        textColumn.setPadding(36, 30, 36, 30)

        // tag badge, only shown if the user actually typed one in
        if (recipe.tag.isNotEmpty()) {
            val tagView = TextView(context)
            tagView.text = recipe.tag
            tagView.setTextColor(Color.parseColor("#085041"))
            tagView.textSize = 12f
            tagView.setBackgroundColor(Color.parseColor("#E8F5E9"))
            tagView.setPadding(28, 10, 28, 10)
            val tagParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tagParams.bottomMargin = 16
            tagView.layoutParams = tagParams
            textColumn.addView(tagView)
        }

        // recipe title, bold and bigger
        val titleView = TextView(context)
        titleView.text = recipe.title
        titleView.setTextColor(Color.parseColor("#1B1B1B"))
        titleView.textSize = 18f
        titleView.setTypeface(null, android.graphics.Typeface.BOLD)
        textColumn.addView(titleView)

        // short ingredients preview, just the first 50 characters so it doesn't overwhelm the card
        val previewView = TextView(context)
        val shortIngredients = if (recipe.ingredients.length > 50) {
            recipe.ingredients.substring(0, 50) + "..."
        } else {
            recipe.ingredients
        }
        previewView.text = shortIngredients
        previewView.setTextColor(Color.parseColor("#666666"))
        previewView.textSize = 13f
        val previewParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        previewParams.topMargin = 12
        previewParams.bottomMargin = 16
        previewView.layoutParams = previewParams
        textColumn.addView(previewView)

        // bottom row: date on the left, delete button on the right
        val bottomRow = LinearLayout(context)
        bottomRow.orientation = LinearLayout.HORIZONTAL
        bottomRow.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val dateView = TextView(context)
        dateView.text = recipe.dateAdded
        dateView.setTextColor(Color.parseColor("#999999"))
        dateView.textSize = 12f
        val dateParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )
        dateView.layoutParams = dateParams
        bottomRow.addView(dateView)

        val deleteButton = TextView(context)
        deleteButton.text = "Delete"
        deleteButton.setTextColor(Color.parseColor("#C0392B"))
        deleteButton.textSize = 13f
        deleteButton.setTypeface(null, android.graphics.Typeface.BOLD)
        deleteButton.setOnClickListener {
            RecipeStorage.deleteRecipe(requireContext(), recipe)
            // refresh the whole list immediately after deleting
            val containerRecipes = view?.findViewById<LinearLayout>(R.id.container_recipes)
            val tvEmptyState = view?.findViewById<TextView>(R.id.tv_empty_state)
            if (containerRecipes != null && tvEmptyState != null) {
                loadAndDisplayRecipes(containerRecipes, tvEmptyState)
            }
        }
        bottomRow.addView(deleteButton)

        textColumn.addView(bottomRow)
        outerColumn.addView(textColumn)
        card.addView(outerColumn)

        return card
    }
}