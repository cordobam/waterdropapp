package com.example.waterdropapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarFragment(UltimosFragment())

        findViewById<BottomNavigationView>(R.id.bottomNav)
            .setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.menu_ultimos -> cargarFragment(UltimosFragment())
                    R.id.menu_cargar -> cargarFragment(CargarRiegoFragment())
                    R.id.menu_plantas -> cargarFragment(PlantasFragment())
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