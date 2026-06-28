package com.lin.ninisoul

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import java.io.File

class RecipeDetailFragment : Fragment() {

    private var recipeTitle: String? = null
    private var recipeIngredients: String? = null
    private var recipeSteps: String? = null
    private var recipeTag: String? = null
    private var recipeDate: String? = null
    private var recipeImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Extracting data passed via the Bundle arguments from the library card click
        arguments?.let {
            recipeTitle = it.getString("title")
            recipeIngredients = it.getString("ingredients")
            recipeSteps = it.getString("steps")
            recipeTag = it.getString("tag")
            recipeDate = it.getString("date")
            recipeImagePath = it.getString("imagePath")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivPhoto = view.findViewById<ImageView>(R.id.iv_detail_photo)
        val tvTag = view.findViewById<TextView>(R.id.tv_detail_tag)
        val tvTitle = view.findViewById<TextView>(R.id.tv_detail_title)
        val tvDate = view.findViewById<TextView>(R.id.tv_detail_date)
        val tvIngredients = view.findViewById<TextView>(R.id.tv_detail_ingredients)
        val tvSteps = view.findViewById<TextView>(R.id.tv_detail_steps)
        val btnEdit = view.findViewById<Button>(R.id.btn_edit_recipe)

        // Inject data into layout elements
        tvTitle.text = recipeTitle
        tvDate.text = recipeDate ?: "Created recently"
        tvIngredients.text = recipeIngredients
        tvSteps.text = recipeSteps

        // Manage optional custom tag display
        if (!recipeTag.isNullOrEmpty()) {
            tvTag.text = recipeTag
            tvTag.visibility = View.VISIBLE
        } else {
            tvTag.visibility = View.GONE
        }

        // Set local image profile URI if available, otherwise default block handles it
        if (!recipeImagePath.isNullOrEmpty() && File(recipeImagePath!!).exists()) {
            ivPhoto.setImageURI(Uri.fromFile(File(recipeImagePath!!)))
        } else {
            ivPhoto.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"))
        }

        // Route directly to AddRecipeFragment to perform editing, passing existing data forward
        btnEdit.setOnClickListener {
            val editBundle = Bundle().apply {
                putString("edit_title", recipeTitle)
                putString("edit_ingredients", recipeIngredients)
                putString("edit_steps", recipeSteps)
                putString("edit_tag", recipeTag)
                putString("edit_imagePath", recipeImagePath)
                putBoolean("is_edit_mode", true)
            }
            findNavController().navigate(R.id.addRecipeFragment, editBundle)
        }
    }
}