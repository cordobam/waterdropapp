package com.example.waterdropapp.data.repository

import android.content.ContentValues
import com.example.waterdropapp.data.local.dto.RiegoHistorialDTO
import com.example.waterdropapp.data.local.dto.RiegosPlantaDTO
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_RIEGOS
import com.example.waterdropapp.data.local.model.UltimoRiego
import com.example.waterdropapp.data.repository.PlantaRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RiegoRepository(private val db: DBHelper) {

    fun putRiegos(planta_id: Int, fecha:String): Long {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("planta_id", planta_id)
            put("fecha", fecha)
        }
        return db.insert(TABLE_NAME_RIEGOS, null, values)
    }

    fun putRiegoPorGrupo(grupoId: Int, fecha: String) {
        val db = db.writableDatabase
        val ids = mutableListOf<Int>()
        val cursor = db.rawQuery("""select gp.planta_id from 
            grupos_plantas gp inner join plantas p ON p.planta_id = gp.planta_id
            where gp.grupo_id = ? and p.activo = 1""", arrayOf(grupoId.toString()))
        //val plantasIds = obtenerEstadoPlantasPorGrupo(grupoId)

        while (cursor.moveToNext()){
            ids.add(cursor.getInt(0))
        }
        cursor.close()

        db.beginTransaction()
        try {
            ids.forEach { id ->
                val values = ContentValues().apply {
                    put("planta_id", id)
                    put("fecha", fecha)
                }
                db.insert(TABLE_NAME_RIEGOS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerRiegosPorPlanta(): List<RiegosPlantaDTO> {
        val db = db.readableDatabase
        val map = mutableMapOf<Int, Pair<Int, MutableList<String>>>()

        val query = """
        SELECT r.planta_id, r.fecha, p.dias_max_sin_riego
        FROM riegos r
        JOIN plantas p ON p.planta_id = r.planta_id
        WHERE p.activo = 1
        ORDER BY r.planta_id, r.fecha ASC
    """

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val plantaId = cursor.getInt(cursor.getColumnIndexOrThrow("planta_id"))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
            val maxDias = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))

            if (!map.containsKey(plantaId)) {
                map[plantaId] = Pair(maxDias, mutableListOf())
            }

            map[plantaId]?.second?.add(fecha)
        }

        cursor.close()

        return map.map {
            RiegosPlantaDTO(
                plantaId = it.key,
                diasMax = it.value.first,
                fechas = it.value.second
            )
        }
    }

    fun obtenerHistorialRiegoxPlanta(planta_id: Int): List<RiegoHistorialDTO> {
        val lista = mutableListOf<RiegoHistorialDTO>()
        val db = db.readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT p.nombre AS nombre_planta,
               r.fecha   AS fecha_riego,
               p.dias_max_sin_riego
        FROM riegos r
        INNER JOIN plantas p ON p.planta_id = r.planta_id
        WHERE r.planta_id = ?
        AND p.activo = 1
        ORDER BY r.fecha DESC
        LIMIT 5
        """,
            arrayOf(planta_id.toString())
        )

        var fechaAnterior: Date? = null


        while (cursor.moveToNext()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre_planta"))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha_riego"))
            val maxDias = cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))
            val fechaActual = parseFecha(fecha)

            val diasDesdeUltimo = fechaAnterior?.let { ((it.time - fechaActual.time) / (1000 * 60 * 60 * 24)).toInt() }
            //val fueraDeRango = diasDesdeUltimo != null && diasDesdeUltimo > maxDias
            val nivelAlerta = when {
                diasDesdeUltimo == null -> 0
                diasDesdeUltimo <= maxDias -> 0
                diasDesdeUltimo <= maxDias + 2 -> 1
                else -> 2
            }

            lista.add(
                RiegoHistorialDTO(
                    nombrePlanta = nombre,
                    fechaRiego = fecha,
                    diasDesdeUltimo = diasDesdeUltimo,
                    alerta = nivelAlerta
                )
            )
            fechaAnterior = fechaActual
        }

        cursor.close()
        return lista
    }

    // metricas y consultas particulares

    fun getUltimosRiegos(): List<UltimoRiego> {
        val lista = mutableListOf<UltimoRiego>()
        val db = db.readableDatabase
        val cursor = db.rawQuery("SELECT p.nombre, MAX(r.fecha)\n"+
                "            FROM riegos r\n"+
                "            LEFT JOIN plantas p ON p.planta_id = r.planta_id\n"+
                "            WHERE p.activo = 1 GROUP BY p.planta_id", null)


        if (cursor.moveToFirst()) {
            do {
                val ur = UltimoRiego()
                ur.name = cursor.getString(0)
                ur.fecha = cursor.getString(1)
                lista.add(ur)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }

    private fun parseFecha(fecha: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.isLenient = false
        return sdf.parse(fecha)!!
    }
}