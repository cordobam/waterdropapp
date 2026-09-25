package com.example.waterdropapp.data.repository

import android.content.ContentValues
import com.example.waterdropapp.data.local.dto.RiegosPlantaDTO
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_ACTIVIDADES
import com.example.waterdropapp.data.local.model.UltimoRiego

class RiegoRepository(private val db: DBHelper) {

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
                    put("tipo", "RIEGO")
                    put("fecha", fecha)
                }
                db.insert(TABLE_NAME_ACTIVIDADES, null, values)
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
        SELECT a.planta_id, a.fecha, p.dias_max_sin_riego
        FROM actividades_planta a
        JOIN plantas p ON p.planta_id = a.planta_id
        WHERE p.activo = 1
        AND a.tipo = 'RIEGO'
        ORDER BY a.planta_id, a.fecha ASC
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

    // metricas y consultas particulares

    fun getUltimosRiegos(): List<UltimoRiego> {
        val lista = mutableListOf<UltimoRiego>()
        val db = db.readableDatabase
        val cursor = db.rawQuery("SELECT p.nombre, MAX(a.fecha)\n"+
                "            FROM actividades_planta a\n"+
                "            LEFT JOIN plantas p ON p.planta_id = a.planta_id\n"+
                "            WHERE a.tipo = 'RIEGO' AND p.activo = 1 GROUP BY p.planta_id", null)


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

}