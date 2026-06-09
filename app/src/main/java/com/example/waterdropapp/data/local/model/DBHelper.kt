package com.example.waterdropapp.data.local.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.local.dto.EstadoGruposDTO
import com.example.waterdropapp.data.local.dto.EstadoPlantasDTO
import com.example.waterdropapp.data.local.dto.RiegoHistorialDTO
import com.example.waterdropapp.data.local.dto.RiegosPlantaDTO
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.util.Date
import java.util.Locale

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTablePlantas = """
            CREATE TABLE $TABLE_NAME_PLANTAS (
                planta_id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                dias_max_sin_riego INTEGER NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1,
                imagen_path TEXT,
                fecha_creacion TEXT,
                dias_max_sin_riego_invierno INTEGER NOT NULL
            )
        """.trimIndent()

        val createTableRiegos = """
            CREATE TABLE $TABLE_NAME_RIEGOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                planta_id INTEGER,
                fecha TEXT NOT NULL,
                FOREIGN KEY(planta_id) REFERENCES plantas(planta_id)
            )
        """.trimIndent()

        val createTableGrupos = """
            CREATE TABLE $TABLE_NAME_GRUPOS (
                grupo_id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()

        val createTableGruposMany = """
            CREATE TABLE $TABLE_NAME_GRUPOS_MANY (
                planta_id INTEGER NOT NULL,
                grupo_id INTEGER NOT NULL,
                PRIMARY KEY (planta_id, grupo_id),
                FOREIGN KEY(planta_id) REFERENCES plantas(planta_id),
                FOREIGN KEY(grupo_id) REFERENCES grupos(grupo_id)
            )
        """.trimIndent()

        val createTableWeatherCache = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME_WEATHER_CACHE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT NOT NULL,
                min_temp REAL,
                max_temp REAL,
                ciudad TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTableGrupos)
        db.execSQL(createTablePlantas)
        db.execSQL(createTableRiegos)
        db.execSQL(createTableGruposMany)
        db.execSQL(createTableWeatherCache)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_GRUPOS_MANY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_RIEGOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PLANTAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_GRUPOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_WEATHER_CACHE")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "plantas.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME_PLANTAS = "plantas"
        const val TABLE_NAME_RIEGOS = "riegos"
        const val TABLE_NAME_GRUPOS = "grupos"
        const val TABLE_NAME_GRUPOS_MANY = "grupos_plantas"
        const val TABLE_NAME_WEATHER_CACHE = "weather_cache"
    }
}