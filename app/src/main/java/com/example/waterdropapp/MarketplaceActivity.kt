package com.example.waterdropapp

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.waterdropapp.data.local.model.DatabaseHelperWeather
import com.example.waterdropapp.data.local.prefs.SeasonPrefs
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.example.waterdropapp.data.repository.SeasonRepository
import com.example.waterdropapp.data.repository.SeasonChangeResult
import com.example.waterdropapp.data.repository.WeatherRepository
import com.example.waterdropapp.ui.ajustes.AjustesFragment
import com.example.waterdropapp.ui.marketplace.ChatsFragment
import com.example.waterdropapp.workers.RiegoWorker
import com.example.waterdropapp.workers.SeasonCheckWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarketplaceActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private var chatListener: com.google.firebase.firestore.ListenerRegistration? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marketplace)

        cargarFragment(MarketplaceFragment())

        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavMarketplace)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_market_inicio -> cargarFragment(MarketplaceFragment())
                R.id.menu_market_viveros -> cargarFragment(ViverosFragment())
                R.id.menu_market_chats -> cargarFragment(ChatsFragment())
                R.id.menu_market_perfil -> cargarFragment(PerfilMarketFragment())
            }
            true
        }

        permisos()
        programarWorkerRiego()
        programarSeasonCheckWorker()
        setupAjustesBadge()
        setupChatBadge()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        setupAjustesBadge()
    }

    override fun onDestroy() {
        super.onDestroy()
        chatListener?.remove()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAjustesBadge() {
        setupAjustesBadge()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupAjustesBadge() {
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = SeasonPrefs.create(this@MarketplaceActivity)
            if (prefs.load().modo == com.example.waterdropapp.domain.model.SeasonMode.AUTO) {
                val weatherDb = DatabaseHelperWeather(this@MarketplaceActivity)
                val weatherRepo = WeatherRepository()
                val seasonRepo = SeasonRepository(weatherDb, weatherRepo, prefs, this@MarketplaceActivity)
                val result = seasonRepo.checkSeasonChange()

                withContext(Dispatchers.Main) {
                    val badge = bottomNav.getOrCreateBadge(R.id.menu_market_perfil)
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
                    val badge = bottomNav.getOrCreateBadge(R.id.menu_market_perfil)
                    badge.isVisible = false
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChatBadge() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val repo = FirestoreRepository()

        chatListener = repo.listenChatsDeUsuario(uid) { chats ->
            val totalNoLeidos = chats.sumOf { it.noLeidos[uid] ?: 0 }
            runOnUiThread {
                val badge = bottomNav.getOrCreateBadge(R.id.menu_market_chats)
                badge.isVisible = totalNoLeidos > 0
                badge.number = totalNoLeidos
            }
        }
    }

    fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorMarketplace, fragment)
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
