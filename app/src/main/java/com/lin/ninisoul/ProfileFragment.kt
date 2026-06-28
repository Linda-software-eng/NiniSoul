package com.lin.ninisoul

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvRecipesCount = view.findViewById<TextView>(R.id.tv_recipes_count)
        val tvHistoryCount = view.findViewById<TextView>(R.id.tv_history_count)
        val btnClearData = view.findViewById<Button>(R.id.btn_clear_data)
        val btnCallSupport = view.findViewById<Button>(R.id.btn_call_support)

        // Load stats
        loadStats(tvRecipesCount, tvHistoryCount)

        // Logic for Calling Support
        btnCallSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:0790160000")
            startActivity(intent)
        }

        // Logic for Clearing Data
        btnClearData.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear all saved data?")
                .setMessage("This will permanently delete all your saved recipes and cooking history. This cannot be undone.")
                .setPositiveButton("Clear Everything") { _, _ ->
                    clearAllData()
                    loadStats(tvRecipesCount, tvHistoryCount)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        val tvRecipesCount = view?.findViewById<TextView>(R.id.tv_recipes_count)
        val tvHistoryCount = view?.findViewById<TextView>(R.id.tv_history_count)
        if (tvRecipesCount != null && tvHistoryCount != null) {
            loadStats(tvRecipesCount, tvHistoryCount)
        }
    }

    private fun loadStats(recipesView: TextView, historyView: TextView) {
        val recipeCount = RecipeStorage.getAllRecipes(requireContext()).size
        recipesView.text = recipeCount.toString()

        val historyCount = RecipeHistoryStorage.getHistoryRecords(requireContext()).size
        historyView.text = historyCount.toString()
    }

    private fun clearAllData() {
        val recipes = RecipeStorage.getAllRecipes(requireContext())
        for (recipe in recipes) {
            RecipeStorage.deleteRecipe(requireContext(), recipe)
        }
        RecipeHistoryStorage.clearHistory(requireContext())
    }
}