package com.example.waterdropapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.data.local.model.DatabaseHelperWeather
import com.example.waterdropapp.data.local.prefs.SeasonPrefs
import com.example.waterdropapp.data.repository.GrupoRepository
import com.example.waterdropapp.data.repository.IndicadoresRepository
import com.example.waterdropapp.data.repository.PlantaRepository
import com.example.waterdropapp.data.repository.RiegoRepository
import com.example.waterdropapp.data.repository.SeasonRepository
import com.example.waterdropapp.data.repository.SeasonChangeResult
import com.example.waterdropapp.data.repository.WeatherRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var plantaRepo: PlantaRepository
    private lateinit var riegoRepo: RiegoRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val helper = DBHelper(requireContext())
        plantaRepo = PlantaRepository(helper, requireContext())
        riegoRepo = RiegoRepository(helper)

        val repository = IndicadoresRepository(plantaRepo, riegoRepo)

        CoroutineScope(Dispatchers.IO).launch {
            val indicadores = repository.getIndicadores()
            
            withContext(Dispatchers.Main) {
                val tvTotal = view.findViewById<TextView>(R.id.tvTotalPlantas)
                val tvTotalxRegar = view.findViewById<TextView>(R.id.tvPorRegar)
                val tvTotalxNoRegar = view.findViewById<TextView>(R.id.tvRegadasHoy)
                val tvPromedioDias = view.findViewById<TextView>(R.id.tvPromedioDias)
                val tvPromedioTardanza = view.findViewById<TextView>(R.id.tvPromedioTardanza)

                tvTotal.text = indicadores.total.toString()
                tvTotalxRegar.text = indicadores.necesitanRiego.toString()
                tvTotalxNoRegar.text = indicadores.noNecesitanRiego.toString()
                tvPromedioDias.text = String.format("%.2f", indicadores.promedioDiasRiego)
                tvPromedioTardanza.text = String.format("%.2f", indicadores.promedioTardanza)
                
                val btnMarketplace = view.findViewById<MaterialButton>(R.id.btnMarketplace)

                btnMarketplace.setOnClickListener {
                    val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    val isLoggedIn = prefs.getBoolean("is_logged_in", false)

                    val intent = if (isLoggedIn) {
                        Intent(requireContext(), MarketplaceActivity::class.java)
                    } else {
                        Intent(requireContext(), LoginActivity::class.java)
                    }
                    startActivity(intent)
                }
            }
        }

        // Clima
        val tvTempMin = view.findViewById<TextView>(R.id.tvTempMin)
        val tvTempMax = view.findViewById<TextView>(R.id.tvTempMax)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weatherRepo = WeatherRepository()
                val prefs = SeasonPrefs.create(requireContext())
                val config = prefs.load()
                val weather = weatherRepo.getWeeklyTemperatures(config.ciudad)

                val minSemana = weather.temperaturas
                    .mapNotNull { it.min }
                    .minOrNull()
                val maxSemana = weather.temperaturas
                    .mapNotNull { it.max }
                    .maxOrNull()

                withContext(Dispatchers.Main) {
                    tvTempMin.text = "${minSemana?.toInt() ?: ""}°"
                    tvTempMax.text = "${maxSemana?.toInt() ?: ""}°"
                }

                // Check for season change
                val weatherDb = DatabaseHelperWeather(requireContext())
                val seasonRepo = SeasonRepository(weatherDb, weatherRepo, prefs, requireContext())
                val result = seasonRepo.checkSeasonChange()

                if (result is SeasonChangeResult.Changed) {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(requireView(),
                            "Detectado cambio de estación: ${result.from?.name ?: "desconocida"} → ${result.to.name}. ¿Configurar?",
                            Snackbar.LENGTH_LONG)
                            .setAction("Ajustes") {
                                (activity as? MainActivity)?.cargarFragment(com.example.waterdropapp.ui.ajustes.AjustesFragment())
                            }
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                }
            }
        }
    }
}