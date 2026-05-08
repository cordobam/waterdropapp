package com.example.waterdropapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.example.waterdropapp.ui.theme.WaterdropappTheme
import com.google.android.material.bottomnavigation.BottomNavigationView

class MarketplaceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marketplace)

        cargarFragment(MarketplaceFragment())

        findViewById<BottomNavigationView>(R.id.bottomNavMarketplace)
            .setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.menu_market_inicio  -> cargarFragment(MarketplaceFragment())
                    R.id.menu_market_viveros -> cargarFragment(ViverosFragment())
                    R.id.menu_market_perfil  -> cargarFragment(PerfilMarketFragment())
                }
                true
            }
    }

    fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorMarketplace, fragment)
            .commit()
    }
}