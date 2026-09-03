package com.example.waterdropapp

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.waterdropapp.data.local.model.DatabaseHelperWeather
import com.example.waterdropapp.data.local.prefs.SeasonPrefs
import com.example.waterdropapp.data.repository.SeasonRepository
import com.example.waterdropapp.data.repository.SeasonChangeResult
import com.example.waterdropapp.data.repository.WeatherRepository
import com.example.waterdropapp.ui.ajustes.AjustesFragment
import com.example.waterdropapp.workers.RiegoWorker
import com.example.waterdropapp.workers.SeasonCheckWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarFragment(HomeFragment())

        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> cargarFragment(HomeFragment())
                R.id.menu_riego -> cargarFragment(PlantasFragment())
                R.id.menu_historial -> cargarFragment(HistorialRiegoFragment())
                R.id.menu_ajustes -> cargarFragment(AjustesFragment())
            }
            true
        }

        permisos()
        programarWorkerRiego()
        programarSeasonCheckWorker()
        setupAjustesBadge()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        setupAjustesBadge()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAjustesBadge() {
        setupAjustesBadge()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupAjustesBadge() {
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = SeasonPrefs.create(this@MainActivity)
            if (prefs.load().modo == com.example.waterdropapp.domain.model.SeasonMode.AUTO) {
                val weatherDb = DatabaseHelperWeather(this@MainActivity)
                val weatherRepo = WeatherRepository()
                val seasonRepo = SeasonRepository(weatherDb, weatherRepo, prefs, this@MainActivity)
                val result = seasonRepo.checkSeasonChange()

                withContext(Dispatchers.Main) {
                    val badge = bottomNav.getOrCreateBadge(R.id.menu_ajustes)
                    when (result) {
                        is SeasonChangeResult.Changed -> {
                            badge.isVisible = true
                            badge.number = 1
                        }
                        SeasonChangeResult.NoChange -> {
                            badge.isVisible = false
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    val badge = bottomNav.getOrCreateBadge(R.id.menu_ajustes)
                    badge.isVisible = false
                }
            }
        }
    }

    internal fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun programarWorkerRiego() {
        val request = PeriodicWorkRequestBuilder<RiegoWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "riego_diario",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun programarSeasonCheckWorker() {
        val request = PeriodicWorkRequestBuilder<SeasonCheckWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(1, TimeUnit.HOURS)
            .addTag("season_check")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "season_check_diario",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun permisos() {
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