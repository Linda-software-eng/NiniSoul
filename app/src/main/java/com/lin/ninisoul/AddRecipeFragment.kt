package com.lin.ninisoul

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// "Add/Edit Recipe" screen, where the user types or updates their own family recipes
// and optionally attaches a photo, then saves everything using RecipeStorage.kt
class AddRecipeFragment : Fragment() {

    // this holds the path to the photo once the user picks one, starts as null (no photo yet)
    private var selectedPhotoPath: String? = null

    // EDIT MODE TRACKERS: I need these to store the recipe values sent over from
    // the RecipeDetailFragment bundle so I can pre-fill the form fields below
    private var isEditMode = false
    private var oldRecipeTitle: String? = null
    private var oldRecipeIngredients: String? = null
    private var oldRecipeSteps: String? = null
    private var oldRecipeTag: String? = null
    private var oldRecipeDate: String? = null

    // this is Android's modern way of asking "open the gallery and let me pick one image"
    // it registers a listener that runs automatically once the user actually picks something
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // once they pick an image, I copy it into my app's own private storage
            // this matters because the original gallery image could get deleted or moved later,
            // but my own private copy will always stay safe and available
            val copiedPath = copyImageToInternalStorage(uri)
            selectedPhotoPath = copiedPath

            // update the UI to show the picked image and hide the "tap to add" hint
            view?.findViewById<ImageView>(R.id.iv_selected_photo)?.apply {
                setImageURI(Uri.fromFile(File(copiedPath)))
                visibility = View.VISIBLE
            }
            view?.findViewById<TextView>(R.id.tv_add_photo_hint)?.visibility = View.GONE
        }
    }

    // copies whatever image the user picked into my app's private internal storage folder
    // returns the new file path so I can save and reload it later
    private fun copyImageToInternalStorage(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        return file.absolutePath
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.et_title)
        val etIngredients = view.findViewById<EditText>(R.id.et_ingredients)
        val etSteps = view.findViewById<EditText>(R.id.et_steps)
        val etTag = view.findViewById<EditText>(R.id.et_tag)
        val btnSaveRecipe = view.findViewById<Button>(R.id.btn_save_recipe)
        val ivSelectedPhoto = view.findViewById<ImageView>(R.id.iv_selected_photo)
        val tvAddPhotoHint = view.findViewById<TextView>(R.id.tv_add_photo_hint)
        val photoCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.card_photo_picker)

        // DETECT EDIT MODE: check if this fragment was launched from the edit button
        // by parsing the structural argument bundles sent over from the detail screen
        arguments?.let {
            isEditMode = it.getBoolean("is_edit_mode", false)
            if (isEditMode) {
                // save the original values into variables so I know what file to match against later
                oldRecipeTitle = it.getString("edit_title")
                oldRecipeIngredients = it.getString("edit_ingredients")
                oldRecipeSteps = it.getString("edit_steps")
                oldRecipeTag = it.getString("edit_tag")
                oldRecipeDate = it.getString("edit_date")
                selectedPhotoPath = it.getString("edit_imagePath")

                // populate the input text boxes with the existing recipe info straight away
                etTitle.setText(oldRecipeTitle)
                etIngredients.setText(oldRecipeIngredients)
                etSteps.setText(oldRecipeSteps)
                etTag.setText(oldRecipeTag)

                // update the button text so the user knows they are updating, not creating new
                btnSaveRecipe.text = "Update Recipe"

                // if the original recipe had a photo path saved, load it up into the preview card
                if (!selectedPhotoPath.isNullOrEmpty() && File(selectedPhotoPath!!).exists()) {
                    ivSelectedPhoto.setImageURI(Uri.fromFile(File(selectedPhotoPath!!)))
                    ivSelectedPhoto.visibility = View.VISIBLE
                    tvAddPhotoHint.visibility = View.GONE
                }
            }
        }

        // tapping the whole photo card opens the gallery picker
        // using its own direct id now, much more reliable than climbing up through parents
        photoCard.setOnClickListener {
            // "image/*" means only let them pick image files, not videos or other file types
            pickImageLauncher.launch("image/*")
        }

        btnSaveRecipe.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val ingredients = etIngredients.text.toString().trim()
            val steps = etSteps.text.toString().trim()
            val tag = etTag.text.toString().trim()

            if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in the recipe name, ingredients and steps", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // if I am editing, keep the original date the recipe was first born.
            // otherwise, generate today's brand new date value stamp string
            val dateToSave = if (isEditMode) {
                oldRecipeDate ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            } else {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            }

            // assemble our structured data object ready for saving
            val updatedRecipe = SavedRecipe(
                title = title,
                ingredients = ingredients,
                steps = steps,
                tag = tag,
                dateAdded = dateToSave,
                photoPath = selectedPhotoPath
            )

            if (isEditMode) {
                // to overwrite, create a mirror model of the old recipe entry using its original parameters
                // so RecipeStorage can find its old file entry name signature and delete it safely
                val oldRecipe = SavedRecipe(
                    title = oldRecipeTitle ?: "",
                    ingredients = oldRecipeIngredients ?: "",
                    steps = oldRecipeSteps ?: "",
                    tag = oldRecipeTag ?: "",
                    dateAdded = dateToSave,
                    photoPath = selectedPhotoPath
                )
                // pass both the old trace identity and new data model directly to our storage editor handler
                RecipeStorage.updateRecipe(requireContext(), oldRecipe, updatedRecipe)
                Toast.makeText(requireContext(), "Recipe updated!", Toast.LENGTH_SHORT).show()
            } else {
                // otherwise, run standard flow to save a completely fresh recipe file entry
                RecipeStorage.saveRecipe(requireContext(), updatedRecipe)
                Toast.makeText(requireContext(), "Recipe saved!", Toast.LENGTH_SHORT).show()
            }

            // exit the form and jump back smoothly to the library lists screen automatically
            findNavController().popBackStack()
        }
    }
}