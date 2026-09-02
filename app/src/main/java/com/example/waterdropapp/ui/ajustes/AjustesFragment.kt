package com.example.waterdropapp.ui.ajustes

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.waterdropapp.MainActivity
import com.example.waterdropapp.R
import com.example.waterdropapp.data.local.model.DatabaseHelperWeather
import com.example.waterdropapp.data.local.prefs.SeasonPrefs
import com.example.waterdropapp.data.repository.SeasonRepository
import com.example.waterdropapp.data.repository.SeasonChangeResult
import com.example.waterdropapp.data.repository.WeatherRepository
import com.example.waterdropapp.databinding.FragmentAjustesBinding
import com.example.waterdropapp.domain.model.Estacion
import com.example.waterdropapp.domain.model.SeasonConfig
import com.example.waterdropapp.domain.model.SeasonMode
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AjustesFragment : Fragment() {

    private var _binding: FragmentAjustesBinding? = null
    private val binding get() = _binding!!

    private lateinit var seasonRepository: SeasonRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAjustesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val weatherDb = DatabaseHelperWeather(requireContext())
        val weatherRepo = WeatherRepository()
        val prefs = SeasonPrefs.create(requireContext())
        seasonRepository = SeasonRepository(weatherDb, weatherRepo, prefs, requireContext())

        loadConfig()
        setupListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadConfig() {
        val config = seasonRepository.getConfig()
        binding.switchAuto.isChecked = config.modo == SeasonMode.AUTO
        binding.etCiudad.setText(config.ciudad)
        binding.etUmbralCalor.setText(config.umbralCalorMax.toString())
        binding.etUmbralFrio.setText(config.umbralFrioMin.toString())

        config.estacionManual?.let { estacion ->
            val radioId = when (estacion) {
                Estacion.PRIMAVERA -> R.id.rbPrimavera
                Estacion.VERANO -> R.id.rbVerano
                Estacion.OTONO -> R.id.rbOtono
                Estacion.INVIERNO -> R.id.rbInvierno
            }
            binding.rgEstacion.check(radioId)
        }

        updateVisibility(config.modo)
        updateInfoDisplay(config.modo)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.switchAuto.setOnCheckedChangeListener { _, isChecked ->
            updateVisibility(if (isChecked) SeasonMode.AUTO else SeasonMode.MANUAL)
        }

        binding.btnDetectarAhora.setOnClickListener {
            detectarAhora()
        }

        binding.btnGuardar.setOnClickListener {
            guardarConfig()
        }
    }

    private fun updateVisibility(modo: SeasonMode) {
        binding.layoutUmbrales.visibility = if (modo == SeasonMode.AUTO) View.VISIBLE else View.GONE
        binding.layoutManual.visibility = if (modo == SeasonMode.MANUAL) View.VISIBLE else View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun detectarAhora() {
        binding.btnDetectarAhora.isEnabled = false
        binding.btnDetectarAhora.text = "Detectando..."

        lifecycleScope.launch(Dispatchers.IO) {
            val config = seasonRepository.getConfig()
            val success = seasonRepository.refreshWeatherCache(config.ciudad)

            val result = withContext(Dispatchers.IO) {
                seasonRepository.checkSeasonChange()
            }

            withContext(Dispatchers.Main) {
                binding.btnDetectarAhora.isEnabled = true
                binding.btnDetectarAhora.text = "Detectar ahora"

                if (success) {
                    when (result) {
                        is SeasonChangeResult.Changed -> {
                            val msg = "Cambio detectado: ${result.from?.name ?: "desconocida"} → ${result.to.name}"
                            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                            updateInfoDisplay(result.to)
                        }
                        SeasonChangeResult.NoChange -> {
                            val config = seasonRepository.getConfig()
                            val currentSeason = seasonRepository.calculateAutoSeasonForDisplay(config.ciudad)
                            Snackbar.make(binding.root, "Sin cambios. Estación actual: ${currentSeason.name}", Snackbar.LENGTH_LONG).show()
                            updateInfoDisplay(currentSeason)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al obtener datos climáticos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateInfoDisplay(modo: SeasonMode) {
        val config = seasonRepository.getConfig()
        if (modo == SeasonMode.MANUAL) {
            config.estacionManual?.let { estacion ->
                binding.tvEstacionActual.text = "Estación actual (manual): ${estacion.name}"
            } ?: run {
                binding.tvEstacionActual.text = "Estación actual (manual): --"
            }
            binding.tvUltimaDeteccion.text = "Modo manual - sin detección automática"
        } else {
            binding.tvUltimaDeteccion.text = "Modo automático - usa últimos 15 días"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateInfoDisplay(season: Estacion) {
        binding.tvEstacionActual.text = "Estación detectada: ${season.name}"
        val config = seasonRepository.getConfig()
        if (config.modo == SeasonMode.AUTO) {
            binding.tvUltimaDeteccion.text = "Última detección: ${season.name}"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun guardarConfig() {
        val ciudad = binding.etCiudad.text.toString().trim()
        if (ciudad.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa una ciudad", Toast.LENGTH_SHORT).show()
            return
        }

        val umbralCalor = binding.etUmbralCalor.text.toString().toDoubleOrNull() ?: 25.0
        val umbralFrio = binding.etUmbralFrio.text.toString().toDoubleOrNull() ?: 5.0

        val modo = if (binding.switchAuto.isChecked) SeasonMode.AUTO else SeasonMode.MANUAL

        val estacionManual = if (modo == SeasonMode.MANUAL) {
            val checkedId = binding.rgEstacion.checkedRadioButtonId
            if (checkedId != -1) {
                val radioButton = binding.root.findViewById<RadioButton>(checkedId)
                radioButton.tag?.toString()?.let { Estacion.valueOf(it) }
            } else {
                null
            }
        } else null

        val config = SeasonConfig(
            modo = modo,
            ciudad = ciudad,
            umbralCalorMax = umbralCalor,
            umbralFrioMin = umbralFrio,
            estacionManual = estacionManual
        )

        seasonRepository.saveConfig(config)

        if (modo == SeasonMode.AUTO) {
            // Forzar check inmediato en modo auto
            lifecycleScope.launch(Dispatchers.IO) {
                val result = seasonRepository.checkSeasonChange()
                withContext(Dispatchers.Main) {
                    when (result) {
                        is SeasonChangeResult.Changed -> {
                            updateInfoDisplay(result.to)
                        }
                        SeasonChangeResult.NoChange -> {
                            val currentSeason = seasonRepository.calculateAutoSeasonForDisplay(ciudad)
                            updateInfoDisplay(currentSeason)
                        }
                    }
                }
            }
        } else {
            updateInfoDisplay(modo)
        }

        Toast.makeText(requireContext(), "Configuración guardada", Toast.LENGTH_SHORT).show()

        // Actualizar badge en MainActivity
        (activity as? MainActivity)?.updateAjustesBadge()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}