package com.example.waterdropapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.local.dto.ActividadPlantaDTO
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_ACTIVIDADES
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_PLANTAS
import com.example.waterdropapp.data.local.model.DatabaseHelperWeather
import com.example.waterdropapp.data.local.prefs.SeasonPrefs
import com.example.waterdropapp.domain.model.Estacion
import com.example.waterdropapp.domain.model.TipoActividad
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class ActividadRepository(
    private val db: DBHelper,
    private val context: Context? = null
) {

    private val seasonRepository: SeasonRepository? by lazy {
        context?.let { ctx ->
            val weatherDb = DatabaseHelperWeather(ctx)
            val weatherRepo = WeatherRepository()
            val prefs = SeasonPrefs.create(ctx)
            SeasonRepository(weatherDb, weatherRepo, prefs, ctx)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun putActividad(
        planta_id: Int,
        tipo: TipoActividad,
        fecha: String,
        nota: String? = null
    ): Long {
        return insertActividad(
            db.writableDatabase,
            planta_id,
            tipo,
            fecha,
            nota,
            resolverEstacion()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun putActividadPorGrupo(
        grupoId: Int,
        tipo: TipoActividad,
        fecha: String,
        nota: String? = null
    ) {
        val db = db.writableDatabase
        val ids = mutableListOf<Int>()
        val cursor = db.rawQuery(
            """select gp.planta_id from
            grupos_plantas gp inner join plantas p ON p.planta_id = gp.planta_id
            where gp.grupo_id = ? and p.activo = 1""",
            arrayOf(grupoId.toString())
        )

        while (cursor.moveToNext()) {
            ids.add(cursor.getInt(0))
        }
        cursor.close()

        val estacion = resolverEstacion()

        db.beginTransaction()
        try {
            ids.forEach { id ->
                insertActividad(db, id, tipo, fecha, nota, estacion)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerActividadesxPlanta(
        plantaId: Int,
        tipo: TipoActividad? = null
    ): List<ActividadPlantaDTO> {
        val db = db.readableDatabase
        val lista = mutableListOf<ActividadPlantaDTO>()

        val filtroTipoSql = if (tipo != null) " AND a.tipo = ?" else ""
        val selectionArgs = if (tipo != null) {
            arrayOf(plantaId.toString(), tipo.tag)
        } else {
            arrayOf(plantaId.toString())
        }

        val cursor = db.rawQuery(
            """
        SELECT a.planta_id,
               p.nombre AS nombre_planta,
               a.tipo,
               a.fecha,
               a.nota,
               a.estacion,
               a.dias_max_usados,
               p.dias_max_sin_riego,
               p.dias_max_sin_riego_invierno
        FROM $TABLE_NAME_ACTIVIDADES a
        INNER JOIN plantas p ON p.planta_id = a.planta_id
        WHERE a.planta_id = ?
        $filtroTipoSql
        AND p.activo = 1
        ORDER BY a.fecha DESC, a.id DESC
        """,
            selectionArgs
        )

        val estacionActual = resolverEstacion()
        var fechaRiegoAnterior: Date? = null

        while (cursor.moveToNext()) {
            val tipoActividad =
                TipoActividad.fromDB(cursor.getString(cursor.getColumnIndexOrThrow("tipo")))

            val maxDiasVerano = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))
            val maxDiasInvierno =
                cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego_invierno"))

            val umbralVigente = when (estacionActual) {
                Estacion.INVIERNO, Estacion.OTONO -> maxDiasInvierno
                else -> maxDiasVerano
            }

            var diasDesdeUltimo: Int? = null
            var alerta = 0
            var umbralDias = umbralVigente
            var estacion: Estacion? = null

            if (tipoActividad == TipoActividad.RIEGO) {
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                val fechaActual = parseFecha(fecha)

                diasDesdeUltimo = fechaRiegoAnterior?.let {
                    ((it.time - fechaActual.time) / (1000 * 60 * 60 * 24)).toInt()
                }

                val idxUmbral = cursor.getColumnIndexOrThrow("dias_max_usados")
                if (!cursor.isNull(idxUmbral)) {
                    umbralDias = cursor.getInt(idxUmbral)
                }

                alerta = when {
                    diasDesdeUltimo == null -> 0
                    diasDesdeUltimo <= umbralDias -> 0
                    diasDesdeUltimo <= umbralDias + 2 -> 1
                    else -> 2
                }

                estacion = cursor.getString(cursor.getColumnIndexOrThrow("estacion"))
                    ?.let { runCatching { Estacion.valueOf(it) }.getOrNull() }

                fechaRiegoAnterior = fechaActual
            }

            lista.add(
                ActividadPlantaDTO(
                    plantaId = cursor.getInt(cursor.getColumnIndexOrThrow("planta_id")),
                    nombrePlanta = cursor.getString(cursor.getColumnIndexOrThrow("nombre_planta")),
                    tipo = tipoActividad,
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    nota = cursor.getString(cursor.getColumnIndexOrThrow("nota")),
                    diasDesdeUltimo = diasDesdeUltimo,
                    alerta = alerta,
                    umbralDias = umbralDias,
                    estacion = estacion
                )
            )
        }

        cursor.close()
        return lista
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun resolverEstacion(): Estacion =
        seasonRepository?.getCurrentSeason() ?: Estacion.porMes(LocalDate.now())

    private fun insertActividad(
        db: SQLiteDatabase,
        plantaId: Int,
        tipo: TipoActividad,
        fecha: String,
        nota: String?,
        estacion: Estacion
    ): Long {
        val values = ContentValues().apply {
            put("planta_id", plantaId)
            put("tipo", tipo.tag)
            put("fecha", fecha)
            put("nota", nota)
            if (tipo == TipoActividad.RIEGO) {
                put("dias_max_usados", umbralDe(db, plantaId, estacion))
                put("estacion", estacion.name)
            }
        }
        return db.insert(TABLE_NAME_ACTIVIDADES, null, values)
    }

    private fun umbralDe(db: SQLiteDatabase, plantaId: Int, estacion: Estacion): Int {
        val cursor = db.rawQuery(
            "SELECT dias_max_sin_riego, dias_max_sin_riego_invierno FROM $TABLE_NAME_PLANTAS WHERE planta_id = ?",
            arrayOf(plantaId.toString())
        )

        val umbral = if (cursor.moveToFirst()) {
            when (estacion) {
                Estacion.INVIERNO, Estacion.OTONO -> cursor.getInt(1)
                else -> cursor.getInt(0)
            }
        } else {
            0
        }

        cursor.close()
        return umbral
    }

    private fun parseFecha(fecha: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.isLenient = false
        return sdf.parse(fecha)!!
    }
}
