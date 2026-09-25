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

        val createTableActividades = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME_ACTIVIDADES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                planta_id INTEGER NOT NULL,
                tipo TEXT NOT NULL,
                fecha TEXT NOT NULL,
                nota TEXT,
                FOREIGN KEY(planta_id) REFERENCES plantas(planta_id)
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
        db.execSQL(createTableActividades)
        db.execSQL(createTableGruposMany)
        db.execSQL(createTableWeatherCache)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_NAME_ACTIVIDADES (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    planta_id INTEGER NOT NULL,
                    tipo TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    nota TEXT,
                    FOREIGN KEY(planta_id) REFERENCES plantas(planta_id)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO actividades_planta (planta_id, tipo, fecha)
                SELECT planta_id, 'RIEGO', fecha FROM riegos
                """.trimIndent()
            )
            db.execSQL("DROP TABLE IF EXISTS riegos")
        }
    }

    companion object {
        const val DATABASE_NAME = "plantas.db"
        const val DATABASE_VERSION = 2
        const val TABLE_NAME_PLANTAS = "plantas"
        const val TABLE_NAME_ACTIVIDADES = "actividades_planta"
        const val TABLE_NAME_GRUPOS = "grupos"
        const val TABLE_NAME_GRUPOS_MANY = "grupos_plantas"
        const val TABLE_NAME_WEATHER_CACHE = "weather_cache"
    }
}