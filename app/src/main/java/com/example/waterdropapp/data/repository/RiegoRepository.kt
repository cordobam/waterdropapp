package com.example.waterdropapp.data.repository

import com.example.waterdropapp.data.local.dto.RiegoConUmbral
import com.example.waterdropapp.data.local.dto.RiegosPlantaDTO
import com.example.waterdropapp.data.local.model.DBHelper

class RiegoRepository(private val db: DBHelper) {

    fun obtenerRiegosPorPlanta(): List<RiegosPlantaDTO> {
        val db = db.readableDatabase
        val mapa = mutableMapOf<Int, MutableList<RiegoConUmbral>>()
        val umbrales = mutableMapOf<Int, Pair<Int, Int>>()

        val query = """
        SELECT a.planta_id, a.fecha, a.dias_max_usados,
               p.dias_max_sin_riego, p.dias_max_sin_riego_invierno
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
            val maxVerano =
                cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego"))
            val maxInvierno =
                cursor.getInt(cursor.getColumnIndexOrThrow("dias_max_sin_riego_invierno"))

            val idxUmbral = cursor.getColumnIndexOrThrow("dias_max_usados")
            val umbralSnapshot = if (cursor.isNull(idxUmbral)) null else cursor.getInt(idxUmbral)

            umbrales[plantaId] = Pair(maxVerano, maxInvierno)
            mapa.getOrPut(plantaId) { mutableListOf() }.add(RiegoConUmbral(fecha, umbralSnapshot))
        }

        cursor.close()

        return mapa.map { (plantaId, riegos) ->
            val umbralesPlanta = umbrales[plantaId] ?: Pair(0, 0)
            RiegosPlantaDTO(
                plantaId = plantaId,
                maxVerano = umbralesPlanta.first,
                maxInvierno = umbralesPlanta.second,
                riegos = riegos
            )
        }
    }
}