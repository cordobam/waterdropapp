package com.example.waterdropapp.data.local.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.waterdropapp.data.local.dto.TemperaturaDiaria

class DatabaseHelperWeather(context: Context) {

    private val db = SQLiteDatabase.openOrCreateDatabase(
        context.getDatabasePath("weather_cache.db"), null
    )

    fun onCreate() {
        db.execSQL("""
                CREATE TABLE IF NOT EXISTS weather_cache (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fecha TEXT NOT NULL,
                    min_temp REAL,
                    max_temp REAL,
                    ciudad TEXT NOT NULL
                )
            """.trimIndent())
    }

    fun insertOrReplace(fecha: String, min: Double?, max: Double?, ciudad: String) {
        val values = ContentValues().apply {
            put("fecha", fecha)
            put("min_temp", min)
            put("max_temp", max)
            put("ciudad", ciudad)
        }
        db.insertWithOnConflict(
            "weather_cache", null, values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getTemperaturasSemana(ciudad: String): List<TemperaturaDiaria> {
        val lista = mutableListOf<TemperaturaDiaria>()
        val cursor = db.rawQuery(
            "SELECT fecha, min_temp, max_temp FROM weather_cache WHERE ciudad = ? ORDER BY fecha",
            arrayOf(ciudad)
        )
        while (cursor.moveToNext()) {
            lista.add(TemperaturaDiaria(
                fecha = cursor.getString(0),
                min = cursor.getDouble(1),
                max = cursor.getDouble(2)
            ))
        }
        cursor.close()
        return lista
    }
}