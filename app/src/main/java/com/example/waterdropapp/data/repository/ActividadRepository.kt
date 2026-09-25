package com.example.waterdropapp.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.waterdropapp.data.local.dto.ActividadPlantaDTO
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_ACTIVIDADES
import com.example.waterdropapp.domain.model.TipoActividad
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActividadRepository(private val db: DBHelper) {

    fun putActividad(
        planta_id: Int,
        tipo: TipoActividad,
        fecha: String,
        nota: String? = null
    ): Long {
        return insertActividad(db.writableDatabase, planta_id, tipo, fecha, nota)
    }

    fun putActividadPorGrupo(
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

        db.beginTransaction()
        try {
            ids.forEach { id ->
                insertActividad(db, id, tipo, fecha, nota)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerActividadesxPlanta(
        plantaId: Int,
        tipo: TipoActividad? = null
    ): List<ActividadPlantaDTO> {
        val db = db.readableDatabase
        val lista = mutableListOf<ActividadPlantaDTO>()

        val cursor = db.rawQuery(
            """
        SELECT a.planta_id,
               p.nombre AS nombre_planta,
               a.tipo,
               a.fecha,
               a.nota,
               p.dias_max_sin_riego
        FROM $TABLE_NAME_ACTIVIDADES a
        INNER JOIN plantas p ON p.planta_id = a.planta_id
        WHERE a.planta_id = ?
        AND (? IS NULL OR a.tipo = ?)
        AND p.activo = 1
        ORDER BY a.fecha DESC, a.id DESC
        """,
            arrayOf(plantaId.toString(), tipo?.tag, tipo?.tag)
        )

        var fechaRiegoAnterior: Date? = null

        while (cursor.moveToNext()) {
            val tipoActividad =
                TipoActividad.fromDB(cursor.getString(cursor.getColumnIndexOrThrow("tipo")))

            var diasDesdeUltimo: Int? = null
            var alerta = 0

            if (tipoActividad == TipoActividad.RIEGO) {
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                val maxDias = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))
                val fechaActual = parseFecha(fecha)

                diasDesdeUltimo = fechaRiegoAnterior?.let {
                    ((it.time - fechaActual.time) / (1000 * 60 * 60 * 24)).toInt()
                }
                alerta = when {
                    diasDesdeUltimo == null -> 0
                    diasDesdeUltimo <= maxDias -> 0
                    diasDesdeUltimo <= maxDias + 2 -> 1
                    else -> 2
                }
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
                    alerta = alerta
                )
            )
        }

        cursor.close()
        return lista
    }

    private fun insertActividad(
        db: SQLiteDatabase,
        plantaId: Int,
        tipo: TipoActividad,
        fecha: String,
        nota: String?
    ): Long {
        val values = ContentValues().apply {
            put("planta_id", plantaId)
            put("tipo", tipo.tag)
            put("fecha", fecha)
            put("nota", nota)
        }
        return db.insert(TABLE_NAME_ACTIVIDADES, null, values)
    }

    private fun parseFecha(fecha: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.isLenient = false
        return sdf.parse(fecha)!!
    }
}