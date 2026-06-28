package com.lin.ninisoul

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AlertDialog

class CookingHistoryFragment : Fragment() {

    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var layoutCardsContainer: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cooking_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
        layoutCardsContainer = view.findViewById(R.id.layout_history_cards_container)

        val btnClearAll = view.findViewById<TextView>(R.id.tv_clear_history)
        btnClearAll?.setOnClickListener {
            RecipeHistoryStorage.clearHistory(requireContext())
            renderHistoryTimeline()
            Toast.makeText(requireContext(), "History cleared!", Toast.LENGTH_SHORT).show()
        }

        renderHistoryTimeline()
    }

    private fun renderHistoryTimeline() {
        val loggedRecords = RecipeHistoryStorage.getHistoryRecords(requireContext())
        layoutCardsContainer.removeAllViews()

        if (loggedRecords.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            layoutEmptyState.visibility = View.GONE
            for (record in loggedRecords) {
                val card = CardView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(16, 16, 16, 16) }
                    setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                    cardElevation = 4f
                }

                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(16, 16, 16, 16)
                }

                val img = ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(200, 200)
                    val imgId = resources.getIdentifier("meal${record.mealId + 1}", "drawable", requireActivity().packageName)
                    setImageResource(if (imgId != 0) imgId else android.R.drawable.ic_menu_gallery)
                }

                val text = TextView(requireContext()).apply {
                    text = "${record.mealName}\n${record.dateCooked}"
                    setPadding(20, 0, 20, 0)
                    setTextColor(Color.BLACK)
                }

                row.addView(img)
                row.addView(text)
                card.addView(row)

                card.setOnClickListener {
                    val fullRecipe = RecipeData.recipes.find { it.name == record.mealName }
                    if (fullRecipe != null) {
                        AlertDialog.Builder(requireContext())
                            .setTitle(fullRecipe.name)
                            .setMessage("Ingredients:\n${fullRecipe.fullIngredients}\n\nInstructions:\n${fullRecipe.steps}")
                            .setPositiveButton("Close", null)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Recipe details not found", Toast.LENGTH_SHORT).show()
                    }
                }
                layoutCardsContainer.addView(card)
            }
        }
    }
}