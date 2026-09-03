package com.example.waterdropapp.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.waterdropapp.domain.model.Estacion
import com.example.waterdropapp.domain.model.SeasonConfig
import com.example.waterdropapp.domain.model.SeasonMode
import com.google.gson.Gson

class SeasonPrefs(private val prefs: SharedPreferences) {

    private val MODE_KEY = "season_mode"
    private val CIUDAD_KEY = "season_ciudad"
    private val UMBRAL_CALOR_KEY = "season_umbral_calor"
    private val UMBRAL_FRIO_KEY = "season_umbral_frio"
    private val ESTACION_MANUAL_KEY = "season_estacion_manual"
    private val LAST_SEASON_KEY = "season_last_detected"
    private val LAST_CHECK_KEY = "season_last_check"

    companion object {
        fun create(context: Context): SeasonPrefs {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            return SeasonPrefs(prefs)
        }
    }

    fun load(): SeasonConfig {
        val modeStr = prefs.getString(MODE_KEY, SeasonMode.AUTO.name) ?: SeasonMode.AUTO.name
        val modo = SeasonMode.valueOf(modeStr)
        
        val ciudad = prefs.getString(CIUDAD_KEY, "Córdoba") ?: "Córdoba"
        val umbralCalor = prefs.getFloat(UMBRAL_CALOR_KEY, 25.0f).toDouble()
        val umbralFrio = prefs.getFloat(UMBRAL_FRIO_KEY, 5.0f).toDouble()
        
        val estacionManualStr = prefs.getString(ESTACION_MANUAL_KEY, null)
        val estacionManual = estacionManualStr?.let { Estacion.valueOf(it) }
        
        return SeasonConfig(
            modo = modo,
            ciudad = ciudad,
            umbralCalorMax = umbralCalor,
            umbralFrioMin = umbralFrio,
            estacionManual = estacionManual
        )
    }

    fun save(config: SeasonConfig) {
        prefs.edit()
            .putString(MODE_KEY, config.modo.name)
            .putString(CIUDAD_KEY, config.ciudad)
            .putFloat(UMBRAL_CALOR_KEY, config.umbralCalorMax.toFloat())
            .putFloat(UMBRAL_FRIO_KEY, config.umbralFrioMin.toFloat())
            .putString(ESTACION_MANUAL_KEY, config.estacionManual?.name)
            .apply()
    }

    fun saveLastDetected(season: Estacion) {
        prefs.edit()
            .putString(LAST_SEASON_KEY, season.name)
            .putLong(LAST_CHECK_KEY, System.currentTimeMillis())
            .apply()
    }

    fun getLastDetected(): Estacion? {
        val seasonStr = prefs.getString(LAST_SEASON_KEY, null)
        return seasonStr?.let { Estacion.valueOf(it) }
    }

    fun shouldCheckNow(): Boolean {
        val lastCheck = prefs.getLong(LAST_CHECK_KEY, 0L)
        val now = System.currentTimeMillis()
        val TWENTY_FOUR_HOURS = 24L * 60 * 60 * 1000
        return (now - lastCheck) >= TWENTY_FOUR_HOURS
    }

    fun markChecked() {
        prefs.edit()
            .putLong(LAST_CHECK_KEY, System.currentTimeMillis())
            .apply()
    }

    fun resetToDefaults() {
        prefs.edit()
            .remove(MODE_KEY)
            .remove(CIUDAD_KEY)
            .remove(UMBRAL_CALOR_KEY)
            .remove(UMBRAL_FRIO_KEY)
            .remove(ESTACION_MANUAL_KEY)
            .remove(LAST_SEASON_KEY)
            .remove(LAST_CHECK_KEY)
            .apply()
    }
}