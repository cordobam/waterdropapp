package com.example.waterdropapp.data.repository

import android.content.ContentValues
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.local.dto.EstadoPlantasDTO
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_GRUPOS_MANY
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_PLANTAS
import com.example.waterdropapp.domain.model.FiltroRiego
import com.example.waterdropapp.domain.model.Estacion
import com.example.waterdropapp.data.local.model.Plantas
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.util.Date
import java.util.Locale

class PlantaRepository(private val db: DBHelper) {

    fun putPlantas(nombre: String , dias: Int , imagenPath: String?, dias_inv: Int): Long {
        val db = db.writableDatabase
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaHoy = formatter.format(Date())
        Log.d("DB_TEST", "Fecha generada: '$fechaHoy'")
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("dias_max_sin_riego", dias)
            put("imagen_path", imagenPath)
            put("fecha_creacion", fechaHoy)
            put("dias_max_sin_riego_invierno", dias_inv)
        }
        return db.insert(TABLE_NAME_PLANTAS, null, values)
    }

    fun eliminarPlantas(planta_id:Int): Int {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("activo" , 0)
        }
        return db.update(
            TABLE_NAME_PLANTAS,
            values,
            "planta_id = ?",                  // WHERE
            arrayOf(planta_id.toString()) )
    }

    fun reactivarPlanta(planta_id:Int): Int {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("activo" , 1)
        }
        return db.update(
            TABLE_NAME_PLANTAS,
            values,
            "planta_id = ?",                  // WHERE
            arrayOf(planta_id.toString()) )
    }

    fun softDeletePlanta(id: Int, activo: Boolean): Int
    {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("activo" , if(activo) 1 else 0)
        }
        return db.update(
            TABLE_NAME_PLANTAS,
            values,
            "planta_id = ?",                  // WHERE
            arrayOf(id.toString()) )
    }



    fun actualizarPlantas(planta_id : Int,nombre: String , dias: Int, imagen_path: String?, dias_inv: Int): Int {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("dias_max_sin_riego", dias)
            put("imagen_path", imagen_path)
            put("dias_max_sin_riego_invierno", dias_inv)
        }
        return db.update(
            TABLE_NAME_PLANTAS,
            values,
            "planta_id = ?",                  // WHERE
            arrayOf(planta_id.toString()))
    }

    fun getPlantas(): List<Pair<Int, String>> {
        val lista = mutableListOf<Pair<Int, String>>()
        val db = db.readableDatabase
        val cursor = db.rawQuery("SELECT planta_id, nombre FROM $TABLE_NAME_PLANTAS WHERE activo = 1", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                lista.add(Pair(id, nombre))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }

    fun getPlantasPorId(plantaId: Int): Plantas? {
        val lista = mutableListOf<Plantas>()
        val db = db.readableDatabase
        val cursor = db.rawQuery("        SELECT p.planta_id, p.nombre, p.dias_max_sin_riego, p.imagen_path, gp.grupo_id, p.dias_max_sin_riego_invierno\n" +
                "        FROM $TABLE_NAME_PLANTAS p\n" +
                "        LEFT JOIN $TABLE_NAME_GRUPOS_MANY gp ON p.planta_id = gp.planta_id\n" +
                "        WHERE p.activo = 1 AND p.planta_id = ?", arrayOf(plantaId.toString()))

        var planta: Plantas? = null

        if (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("planta_id"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val dias_max_sin_riego = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))
            val imagen_path = cursor.getString(cursor.getColumnIndexOrThrow("imagen_path"))
            val grupoId = cursor.getInt(cursor.getColumnIndexOrThrow("grupo_id"))
            val dias_max_sin_riego_invierno = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego_invierno"))

            planta =
                Plantas(
                    planta_id = id,
                    nombre = nombre,
                    dias_max_sin_riego = dias_max_sin_riego,
                    activo = null,
                    imagen_path = imagen_path,
                    grupo_id = grupoId,
                    dias_max_sin_riego_invierno= dias_max_sin_riego_invierno
                )
        }

        cursor.close()
        return planta
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerEstadoPlantas(): List<EstadoPlantasDTO> {
        val lista = mutableListOf<EstadoPlantasDTO>()
        val db = db.readableDatabase

        val query = """
        SELECT 
            p.planta_id,
            p.nombre,
            p.dias_max_sin_riego,
            p.dias_max_sin_riego_invierno,
            p.fecha_creacion,
            imagen_path,
            -- Último riego calculado aparte
            (
                SELECT MAX(r.fecha)
                FROM riegos r
                WHERE r.planta_id = p.planta_id
            ) AS ultimo_riego,
        
            -- Grupos calculados aparte
            (
                SELECT GROUP_CONCAT(g.nombre, ', ')
                FROM grupos_plantas gp
                JOIN grupos g ON g.grupo_id = gp.grupo_id
                WHERE gp.planta_id = p.planta_id
            ) AS nombre_grupos
        
        FROM plantas p
        WHERE p.activo = 1
        ORDER BY p.nombre
        """

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("planta_id"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val nombreGrupos = cursor.getString(cursor.getColumnIndexOrThrow("nombre_grupos"))
            val maxDias = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))
            val ultimo = cursor.getString(cursor.getColumnIndexOrThrow("ultimo_riego"))
            val imagenPath = cursor.getString(cursor.getColumnIndexOrThrow("imagen_path"))
            val fecha_creacion = cursor.getString(cursor.getColumnIndexOrThrow("fecha_creacion"))
            val maxDias_invierno = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego_invierno"))

            val fechaBase = if (!ultimo.isNullOrEmpty()) ultimo else fecha_creacion
            val diasSinRegar = calcularDias(fechaBase)

            val estacionActual = calcularEstacion(LocalDate.now())
            var necesita = false
            when (estacionActual) {
                Estacion.VERANO -> {
                    necesita = diasSinRegar >= maxDias
                }
                Estacion.INVIERNO , Estacion.OTONO -> {
                    necesita = diasSinRegar >= maxDias_invierno
                }
                Estacion.PRIMAVERA -> {
                    necesita = diasSinRegar >= maxDias
                }
            }

            lista.add(
                EstadoPlantasDTO(
                    plantaId = id,
                    nombre = nombre,
                    ultimoRiego = ultimo,
                    diasSinRegar = diasSinRegar,
                    necesitaRiego = necesita,
                    nombreGrupos = nombreGrupos,
                    imagen_path = imagenPath,
                    max_dias = maxDias
                )
            )
        }

        cursor.close()
        return lista
    }

    fun obtenerEstadoPlantasPorGrupo(grupoId: Int): List<EstadoPlantasDTO> {
        val lista = mutableListOf<EstadoPlantasDTO>()
        val db = db.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT            
                p.planta_id,
                p.nombre,
                p.dias_max_sin_riego,
                imagen_path,
                -- Último riego
                (
                    SELECT MAX(r.fecha)
                    FROM riegos r
                    WHERE r.planta_id = p.planta_id
                ) AS ultimo_riego,
            
                -- Todos los grupos de la planta
                (
                    SELECT GROUP_CONCAT(g.nombre, ', ')
                    FROM grupos_plantas gp2
                    JOIN grupos g ON g.grupo_id = gp2.grupo_id
                    WHERE gp2.planta_id = p.planta_id
                ) AS nombre_grupos
            
            FROM plantas p
            
            WHERE p.activo = 1
            AND EXISTS (
                SELECT 1
                FROM grupos_plantas gp
                WHERE gp.planta_id = p.planta_id
                AND gp.grupo_id = ?
            )
            
            ORDER BY p.nombre
        """,
            arrayOf(grupoId.toString())
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("planta_id"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val nombreGrupos = cursor.getString(cursor.getColumnIndexOrThrow("nombre_grupos"))
            val maxDias = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))
            val ultimo = cursor.getString(cursor.getColumnIndexOrThrow("ultimo_riego"))
            val imagenPath = cursor.getString(cursor.getColumnIndexOrThrow("imagen_path"))
            val diasSinRegar = calcularDias(ultimo)
            val necesita = diasSinRegar >= maxDias

            lista.add(
                EstadoPlantasDTO(
                    plantaId = id,
                    nombre = nombre,
                    ultimoRiego = ultimo,
                    diasSinRegar = diasSinRegar,
                    necesitaRiego = necesita,
                    nombreGrupos = nombreGrupos,
                    imagen_path = imagenPath,
                    max_dias = maxDias
                )
            )
        }

        cursor.close()
        return lista
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerEstadoPlantasXRiego(
        filtro: FiltroRiego = FiltroRiego.TODAS,
        diasUmbral: Int = 2
    ): List<EstadoPlantasDTO> {

        val lista = obtenerEstadoPlantas()

        return when (filtro){
            FiltroRiego.TODAS -> lista

            FiltroRiego.VENCIDAS ->
                lista.filter { it.necesitaRiego }

            FiltroRiego.PROXIMAS ->
                lista.filter {
                    !it.necesitaRiego &&
                            it.diasSinRegar >= (it.max_dias?.minus(diasUmbral) ?: 0)
                }
        }
    }

    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun calcularDias(fecha: String?): Int {
        if (fecha.isNullOrEmpty()) return 0

        val fechaRiego = try {
            formatter.parse(fecha)
        } catch (e: Exception) {
            return 0
        }

        val hoy = Date()
        val diff = hoy.time - (fechaRiego?.time ?: return 0)

        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularEstacion(fechaHoy: LocalDate): Estacion {
        val dia = fechaHoy.dayOfMonth
        val mes = fechaHoy.month

        // Usamos las fechas aproximadas de los cambios de estación (día 21)
        return when (mes) {
            Month.JANUARY, Month.FEBRUARY -> Estacion.VERANO
            Month.MARCH -> if (dia < 21) Estacion.VERANO else Estacion.OTONO
            Month.APRIL, Month.MAY -> Estacion.OTONO
            Month.JUNE -> if (dia < 21) Estacion.OTONO else Estacion.INVIERNO
            Month.JULY, Month.AUGUST -> Estacion.INVIERNO
            Month.SEPTEMBER -> if (dia < 21) Estacion.INVIERNO else Estacion.PRIMAVERA
            Month.OCTOBER, Month.NOVEMBER -> Estacion.PRIMAVERA
            Month.DECEMBER -> if (dia < 21) Estacion.PRIMAVERA else Estacion.VERANO
        }
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