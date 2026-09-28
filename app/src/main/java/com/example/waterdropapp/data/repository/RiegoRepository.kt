package com.example.waterdropapp.data.repository

import com.example.waterdropapp.data.local.dto.RiegosPlantaDTO
import com.example.waterdropapp.data.local.model.DBHelper

class RiegoRepository(private val db: DBHelper) {

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
}