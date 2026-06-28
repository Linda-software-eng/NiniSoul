package com.lin.ninisoul

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class MealDeciderFragment : Fragment() {

    private lateinit var tvMealName: TextView
    private lateinit var tvMealDescription: TextView
    private lateinit var ivMealImage: ImageView
    private var currentMealIndex: Int = 0
    private val blockedMealIndices = mutableListOf<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_meal_decider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        tvMealName = view.findViewById(R.id.tv_meal_name)
        tvMealDescription = view.findViewById(R.id.tv_meal_description)
        ivMealImage = view.findViewById(R.id.iv_meal_image)

        // Initial setup
        showRandomMeal()

        // Button: Decide
        view.findViewById<Button>(R.id.btn_decide).setOnClickListener {
            showRandomMeal()
        }

        // Button: Don't Suggest (Block)
        view.findViewById<Button>(R.id.btn_block_meal).setOnClickListener {
            blockedMealIndices.add(currentMealIndex)
            showRandomMeal()
        }

        // Button: Pick This Meal
        view.findViewById<Button>(R.id.btn_pick_meal).setOnClickListener {
            RecipeHistoryStorage.logMealSelection(requireContext(), currentMealIndex, tvMealName.text.toString())
            Toast.makeText(requireContext(), "Saved to history!", Toast.LENGTH_SHORT).show()
        }

        // Text: Reset Filters (Top right header)
        view.findViewById<TextView>(R.id.tv_reset_filters).setOnClickListener {
            blockedMealIndices.clear()
            showRandomMeal()
            Toast.makeText(requireContext(), "Filters reset!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRandomMeal() {
        val availableIndices = MealData.meals.indices.filter { it !in blockedMealIndices }

        if (availableIndices.isEmpty()) {
            blockedMealIndices.clear()
            showRandomMeal()
            return
        }

        currentMealIndex = availableIndices.random()
        val randomMeal = MealData.meals[currentMealIndex]

        tvMealName.text = randomMeal.name
        tvMealDescription.text = randomMeal.description

        val imageId = resources.getIdentifier("meal${currentMealIndex + 1}", "drawable", requireActivity().packageName)
        ivMealImage.setImageResource(if (imageId != 0) imageId else android.R.drawable.ic_menu_gallery)
    }
}