package com.example.myapplication.ui.home


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_gps -> loadFragment(GPSFragment())
                R.id.nav_drawing -> loadFragment(DrawingFragment())
                R.id.nav_form -> loadFragment(FormFragment())
                R.id.nav_new_form -> loadFragment(NewFormFragment()) // Manejo del nuevo formulario
            }
            true
        }

        // Cargar el fragmento inicial
        loadFragment(GPSFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
