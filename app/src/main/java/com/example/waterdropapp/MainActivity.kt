package com.example.waterdropapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.work.*
import com.example.waterdropapp.workers.RiegoWorker
import java.util.concurrent.TimeUnit

import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarFragment(PlantasFragment())

        findViewById<BottomNavigationView>(R.id.bottomNav)
            .setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.menu_riego -> cargarFragment(PlantasFragment())
                    R.id.menu_historial -> cargarFragment(HistorialRiegoFragment())
                    R.id.menu_plantas_grupos -> cargarFragment(CargasVariasFragment())

                }
                true
            }

        permisos()
        programarWorkerRiego()

    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun programarWorkerRiego() {

        val request = PeriodicWorkRequestBuilder<RiegoWorker>(
            1, TimeUnit.DAYS
            //1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "riego_diario",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun permisos(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}