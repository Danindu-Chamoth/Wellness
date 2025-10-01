package com.example.wellness

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.wellness.Fragmens.DashboardFragment
import com.example.wellness.Fragmens.HabitsFragment
import com.example.wellness.Fragmens.MoodsFragment
import com.example.wellness.Fragmens.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
        }

        // Set up bottom navigation listener (using updated method)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_dashboard -> {
                    replaceFragment(DashboardFragment())
                    true
                }
                R.id.navigation_habits -> {
                    replaceFragment(HabitsFragment())
                    true
                }
                R.id.navigation_moods -> {
                    replaceFragment(MoodsFragment())
                    true
                }
                R.id.navigation_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}