package com.example.waterdropapp.data.repository

import android.content.Context
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.local.dto.TemperaturaDiaria
import com.example.waterdropapp.data.local.model.DatabaseHelperWeather
import com.example.waterdropapp.data.local.prefs.SeasonPrefs
import com.example.waterdropapp.domain.model.Estacion
import com.example.waterdropapp.domain.model.SeasonConfig
import com.example.waterdropapp.domain.model.SeasonMode
import java.time.LocalDate

class SeasonRepository(
    private val weatherDb: DatabaseHelperWeather,
    private val weatherRepo: WeatherRepository,
    private val prefs: SeasonPrefs,
    private val context: Context
) {

    @RequiresApi(android.os.Build.VERSION_CODES.O)
    suspend fun getCurrentSeason(): Estacion {
        val config = prefs.load()
        return if (config.modo == SeasonMode.MANUAL) {
            config.estacionManual ?: calculateAutoSeason(config.ciudad)
        } else {
            calculateAutoSeason(config.ciudad)
        }
    }

    @RequiresApi(android.os.Build.VERSION_CODES.O)
    suspend fun checkSeasonChange(): SeasonChangeResult {
        val config = prefs.load()
        if (config.modo == SeasonMode.MANUAL) {
            return SeasonChangeResult.NoChange
        }

        val newSeason = calculateAutoSeason(config.ciudad)
        val lastSeason = prefs.getLastDetected()

        if (lastSeason != newSeason) {
            prefs.saveLastDetected(newSeason)
            return SeasonChangeResult.Changed(lastSeason, newSeason)
        }
        return SeasonChangeResult.NoChange
    }

    @RequiresApi(android.os.Build.VERSION_CODES.O)
    private suspend fun calculateAutoSeason(ciudad: String): Estacion {
        val temps = weatherDb.getLast15Days(ciudad)

        if (temps.size < 15) {
            return calcularEstacionPorMes(LocalDate.now())
        }

        val validTemps = temps.filter { it.max != null || it.min != null }
        if (validTemps.size < 10) {
            return calcularEstacionPorMes(LocalDate.now())
        }

        val avgMax = validTemps.mapNotNull { it.max }.average()
        val avgMin = validTemps.mapNotNull { it.min }.average()
        val config = prefs.load()

        return when {
            avgMax >= config.umbralCalorMax -> Estacion.VERANO
            avgMin <= config.umbralFrioMin -> Estacion.INVIERNO
            else -> calcularEstacionPorMes(LocalDate.now())
        }
    }

    @RequiresApi(android.os.Build.VERSION_CODES.O)
    private fun calcularEstacionPorMes(fechaHoy: LocalDate): Estacion =
        Estacion.porMes(fechaHoy)

    @RequiresApi(android.os.Build.VERSION_CODES.O)
    suspend fun refreshWeatherCache(ciudad: String): Boolean {
        return try {
            val weather = weatherRepo.getWeeklyTemperatures(ciudad, weatherDb)
            true
        } catch (e: Exception) {
            false
        }
    }

    @RequiresApi(android.os.Build.VERSION_CODES.O)
    fun calculateAutoSeasonForDisplay(ciudad: String): Estacion {
        val temps = weatherDb.getLast15Days(ciudad)
        if (temps.size < 15) {
            return calcularEstacionPorMes(LocalDate.now())
        }
        val validTemps = temps.filter { it.max != null || it.min != null }
        if (validTemps.size < 10) {
            return calcularEstacionPorMes(LocalDate.now())
        }
        val avgMax = validTemps.mapNotNull { it.max }.average()
        val avgMin = validTemps.mapNotNull { it.min }.average()
        val config = prefs.load()
        return when {
            avgMax >= config.umbralCalorMax -> Estacion.VERANO
            avgMin <= config.umbralFrioMin -> Estacion.INVIERNO
            else -> calcularEstacionPorMes(LocalDate.now())
        }
    }

    fun getConfig(): SeasonConfig = prefs.load()

    fun saveConfig(config: SeasonConfig) {
        prefs.save(config)
    }
}

sealed class SeasonChangeResult {
    data class Changed(
        val from: Estacion?,
        val to: Estacion
    ) : SeasonChangeResult()

    object NoChange : SeasonChangeResult()
}