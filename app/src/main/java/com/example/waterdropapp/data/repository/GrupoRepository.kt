package com.example.waterdropapp.data.repository

import android.content.ContentValues
import com.example.waterdropapp.data.local.dto.EstadoGruposDTO
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_GRUPOS
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_GRUPOS_MANY
import com.example.waterdropapp.data.local.model.DBHelper.Companion.TABLE_NAME_PLANTAS
import com.example.waterdropapp.data.local.model.Grupos
import com.example.waterdropapp.data.local.model.UltimoRiego

class GrupoRepository(private val db: DBHelper) {

    fun putGrupos(nombre: String):Long{
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("nombre" , nombre)
        }
        return db.insert(TABLE_NAME_GRUPOS,null,values)
    }

    fun eliminarGrupos(grupoId: Int): Int{
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("activo" , 0)
        }
        return db.update(
            TABLE_NAME_GRUPOS,
            values,
            "grupo_id = ?",                  // WHERE
            arrayOf(grupoId.toString()) )
    }

    fun reactivarGrupo(grupoId: Int): Int{
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("activo" , 1)
        }
        return db.update(
            TABLE_NAME_GRUPOS,
            values,
            "grupo_id = ?",                  // WHERE
            arrayOf(grupoId.toString()) )
    }

    fun softDeleteGrupo(id: Int, activo: Boolean): Int
    {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("activo" , if(activo) 1 else 0)
        }
        return db.update(
            TABLE_NAME_GRUPOS,
            values,
            "grupo_id = ?",                  // WHERE
            arrayOf(id.toString()) )
    }

    fun actualizarGrupos(grupoId: Int, nombre:String):Int{
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("nombre" , nombre)
        }
        return db.update(
            TABLE_NAME_GRUPOS,
            values,
            "grupo_id = ?",                  // WHERE
            arrayOf(grupoId.toString()))
    }

    fun actualizarGrupoPlanta(plantaId: Int, grupoId: Int) {

        val db = db.writableDatabase

        val values = ContentValues().apply {
            put("grupo_id", grupoId)
        }

        db.update(
            "grupos_plantas",
            values,
            "planta_id = ?",
            arrayOf(plantaId.toString())
        )
    }

    fun getGrupos(): List<Grupos> {
        val lista = mutableListOf<Grupos>()
        val db = db.readableDatabase
        val cursor = db.rawQuery("SELECT grupo_id, nombre FROM $TABLE_NAME_GRUPOS WHERE activo = 1", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("grupo_id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                lista.add(
                    Grupos(
                        grupo_id = id,
                        nombre = nombre
                    )

                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }
    fun getEstadosGrupos(): List<EstadoGruposDTO> {
        val lista = mutableListOf<EstadoGruposDTO>()
        val db = db.readableDatabase
        val cursor = db.rawQuery("SELECT G.grupo_id, G.nombre, count(GP.planta_id) as cantPlantasGrupo  \n" +
                "FROM $TABLE_NAME_GRUPOS G LEFT JOIN $TABLE_NAME_GRUPOS_MANY GP ON G.grupo_id = GP.grupo_id \n" +
                "INNER JOIN $TABLE_NAME_PLANTAS P ON GP.planta_id = P.planta_id WHERE P.activo= 1 \n" +
                "GROUP BY G.grupo_id , G.nombre", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("grupo_id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantPlantasGrupo"))
                lista.add(
                    EstadoGruposDTO(
                        grupoId = id,
                        nombreGrupo = nombre,
                        cantPlantasGrupo = cantidad
                    )

                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }


    // metodos grupos plantas
    fun putGruposPlantas(planta_id: Int, grupo_id:Int): Long {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put("planta_id", planta_id)
            put("grupo_id", grupo_id)
        }
        return db.insert(TABLE_NAME_GRUPOS_MANY, null, values)
    }


}