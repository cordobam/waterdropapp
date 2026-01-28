package com.example.waterdropapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarFragment(PlantasFragment())

        findViewById<BottomNavigationView>(R.id.bottomNav)
            .setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.menu_riego -> cargarFragment(PlantasFragment())
                    R.id.menu_historial -> cargarFragment(UltimosFragment())
                    R.id.menu_plantas -> cargarFragment(CargarPlantasFragment())
                }
                true
            }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}