package com.example.waterdropapp.ui.ajustes

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
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
    private lateinit var safeContext: Context

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

        safeContext = requireContext()

        val weatherDb = DatabaseHelperWeather(safeContext)
        val weatherRepo = WeatherRepository()
        val prefs = SeasonPrefs.create(safeContext)
        seasonRepository = SeasonRepository(weatherDb, weatherRepo, prefs, safeContext)

        loadConfig()
        cargarTemaActual()
        setupListeners()
    }

    private fun cargarTemaActual() {
        val modo = safeContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val radioId = when (modo) {
            AppCompatDelegate.MODE_NIGHT_NO -> R.id.rbTemaClaro
            AppCompatDelegate.MODE_NIGHT_YES -> R.id.rbTemaOscuro
            else -> R.id.rbTemaSistema
        }
        binding.rgTemaApp.check(radioId)
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

        binding.rgTemaApp.setOnCheckedChangeListener { _, checkedId ->
            val modo = when (checkedId) {
                R.id.rbTemaClaro -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbTemaOscuro -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            safeContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("night_mode", modo)
                .apply()
            AppCompatDelegate.setDefaultNightMode(modo)
            requireActivity().recreate()
        }
    }

    private fun updateVisibility(modo: SeasonMode) {
        binding.layoutUmbrales.visibility = if (modo == SeasonMode.AUTO) View.VISIBLE else View.GONE
        binding.layoutManual.visibility = if (modo == SeasonMode.MANUAL) View.VISIBLE else View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun detectarAhora() {
        binding.btnDetectarAhora.isEnabled = false
        binding.btnDetectarAhora.text = getString(R.string.ajustes_detecting)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val config = seasonRepository.getConfig()
            val success = seasonRepository.refreshWeatherCache(config.ciudad)

            val result = withContext(Dispatchers.IO) {
                seasonRepository.checkSeasonChange()
            }

            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
                binding.btnDetectarAhora.isEnabled = true
                binding.btnDetectarAhora.text = safeContext.getString(R.string.ajustes_detect_now)

                if (success) {
                    when (result) {
                        is SeasonChangeResult.Changed -> {
                            val fromStr = result.from?.let { safeContext.getString(seasonRes(it)) }
                                ?: safeContext.getString(R.string.ajustes_unknown_season)
                            val msg = safeContext.getString(R.string.ajustes_season_change, fromStr, safeContext.getString(seasonRes(result.to)))
                            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                            updateInfoDisplay(result.to)
                        }
                        SeasonChangeResult.NoChange -> {
                            val config = seasonRepository.getConfig()
                            val currentSeason = seasonRepository.calculateAutoSeasonForDisplay(config.ciudad)
                            Snackbar.make(binding.root, safeContext.getString(R.string.ajustes_no_change, safeContext.getString(seasonRes(currentSeason))), Snackbar.LENGTH_LONG).show()
                            updateInfoDisplay(currentSeason)
                        }
                    }
                } else {
                    Toast.makeText(safeContext, safeContext.getString(R.string.ajustes_weather_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateInfoDisplay(modo: SeasonMode) {
        val config = seasonRepository.getConfig()
        if (modo == SeasonMode.MANUAL) {
            config.estacionManual?.let { estacion ->
                binding.tvEstacionActual.text = getString(R.string.ajustes_current_manual, getString(seasonRes(estacion)))
            } ?: run {
                binding.tvEstacionActual.text = getString(R.string.ajustes_current_manual, "--")
            }
            binding.tvUltimaDeteccion.text = getString(R.string.ajustes_mode_manual)
        } else {
            binding.tvUltimaDeteccion.text = getString(R.string.ajustes_mode_auto)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateInfoDisplay(season: Estacion) {
        binding.tvEstacionActual.text = getString(R.string.ajustes_season_detected, getString(seasonRes(season)))
        val config = seasonRepository.getConfig()
        if (config.modo == SeasonMode.AUTO) {
            binding.tvUltimaDeteccion.text = getString(R.string.ajustes_last_detection, getString(seasonRes(season)))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun guardarConfig() {
        val ciudad = binding.etCiudad.text.toString().trim()
        if (ciudad.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.ajustes_enter_city), Toast.LENGTH_SHORT).show()
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
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
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

        Toast.makeText(requireContext(), getString(R.string.ajustes_config_saved), Toast.LENGTH_SHORT).show()

        // Actualizar badge en MainActivity
        (activity as? MainActivity)?.updateAjustesBadge()
    }

    private fun seasonRes(estacion: Estacion): Int = when (estacion) {
        Estacion.PRIMAVERA -> R.string.season_primavera
        Estacion.VERANO -> R.string.season_verano
        Estacion.OTONO -> R.string.season_otono
        Estacion.INVIERNO -> R.string.season_invierno
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}