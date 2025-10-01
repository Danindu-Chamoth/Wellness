package com.example.wellness.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
        }

        val titleText = TextView(requireContext()).apply {
            text = "Dashboard"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }

        val descriptionText = TextView(requireContext()).apply {
            text = "Welcome to your wellness dashboard!\nThis feature is coming soon."
            textSize = 16f
        }

        layout.addView(titleText)
        layout.addView(descriptionText)

        return layout
    }
}
